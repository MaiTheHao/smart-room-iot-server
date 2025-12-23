package com.iviet.ivshs.controller.view;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainControllerV1 {

    private final MessageSource messageSource;
    private final FloorServiceV1 floorService;
    private final RoomServiceV1 roomService;
    private final LightServiceV1 lightService;
    private final DeviceControlServiceV1 deviceControlService;
    private final TemperatureServiceV1 temperatureService;
    private final PowerConsumptionServiceV1 powerConsumptionService;

    private static final String LANGUAGE_ATTR = "currentlanguage";
    private static final String WELCOME_MSG_KEY = "msg.welcome";

    @GetMapping("/")
    public String main() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        Locale locale = LocaleContextHolder.getLocale();
        model.addAttribute("messagewelcome", messageSource.getMessage(WELCOME_MSG_KEY, null, locale));
        model.addAttribute(LANGUAGE_ATTR, locale);

        List<FloorDtoV1> floors = floorService.getList(0, 1000).content();
        
        // Map Floor -> List<Room>
        Map<FloorDtoV1, List<RoomDtoV1>> floorRoomsMap = floors.stream()
                .collect(Collectors.toMap(
                        f -> f,
                        f -> roomService.getListByFloor(f.id(), 0, 1000).content(),
                        (oldV, newV) -> oldV,
                        LinkedHashMap::new
                ));

        // Logic thời gian: 15 phút gần nhất
        Instant endedAt = Instant.now();
        Instant startedAt = endedAt.minus(15, ChronoUnit.MINUTES);

        List<RoomDtoV1> allRooms = floorRoomsMap.values().stream()
                .flatMap(List::stream)
                .toList();

        // Thu thập dữ liệu cho từng phòng
        Map<Long, Long> roomGatewayCountMap = new HashMap<>();
        Map<Long, Double> roomCurrentTempMap = new HashMap<>();
        Map<Long, Double> roomCurrentPowerMap = new HashMap<>();

        allRooms.forEach(room -> {
            Long rId = room.id();
            roomGatewayCountMap.put(rId, deviceControlService.countByRoomId(rId));

            // Lấy nhiệt độ cuối cùng trong 15p qua
            var tempHist = temperatureService.getAverageValueHistoryByRoomId(rId, startedAt, endedAt);
            roomCurrentTempMap.put(rId, tempHist.isEmpty() ? 0.0 : tempHist.get(tempHist.size() - 1).avgTempC());

            // Lấy điện năng cuối cùng trong 15p qua
            var powerHist = powerConsumptionService.getSumValueHistoryByRoomId(rId, startedAt, endedAt);
            roomCurrentPowerMap.put(rId, powerHist.isEmpty() ? 0.0 : powerHist.get(powerHist.size() - 1).getSumWattHour());
        });

        model.addAttribute("floorRoomsMap", floorRoomsMap);
        model.addAttribute("roomGatewayCountMap", roomGatewayCountMap);
        model.addAttribute("roomCurrentTempMap", roomCurrentTempMap);
        model.addAttribute("roomCurrentPowerMap", roomCurrentPowerMap);

        return "pages/home.html";
    }

    @GetMapping("/room/{id}")
    public String roomDetail(
            @PathVariable(name = "id") Long roomId,
            @RequestParam(name = "startedAt", required = false) String startedAtStr,
            @RequestParam(name = "endedAt", required = false) String endedAtStr,
            Model model) {
        try {
            RoomDtoV1 room = roomService.getById(roomId);
            model.addAttribute("room", room);
            model.addAttribute("pageTitle", room.name());

            // Khoảng thời gian mặc định
            Instant now = Instant.now();
            Instant defaultStart = now.minus(15, ChronoUnit.MINUTES);

            // Parse thời gian từ params
            Instant chartStart = parseInstant(startedAtStr, defaultStart);
            Instant chartEnd = parseInstant(endedAtStr, now);

            if (chartStart.isAfter(chartEnd)) {
                Instant temp = chartStart; chartStart = chartEnd; chartEnd = temp;
            }

            // Lấy data cho Dashboard & Charts
            var tempChartData = temperatureService.getAverageValueHistoryByRoomId(roomId, chartStart, chartEnd);
            var powerChartData = powerConsumptionService.getSumValueHistoryByRoomId(roomId, chartStart, chartEnd);
            var lights = lightService.getListByRoomId(roomId, 0, 1000).content();

            model.addAttribute("currentTemp", tempChartData.isEmpty() ? 0.0 : tempChartData.get(tempChartData.size() - 1).avgTempC());
            model.addAttribute("currentPower", powerChartData.isEmpty() ? 0.0 : powerChartData.get(powerChartData.size() - 1).getSumWattHour());
            model.addAttribute("tempChartData", tempChartData);
            model.addAttribute("powerChartData", powerChartData);
            model.addAttribute("lights", lights);

            return "pages/room_detail.html";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "pages/room_detail.html";
        }
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            Model model) {
        if (error != null) {
            Locale locale = LocaleContextHolder.getLocale();
            Object exception = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            String messageKey = "login.error.unknown";

            if (exception instanceof AuthenticationException authEx) {
                String msg = authEx.getMessage();
                if (msg.contains("Bad credentials")) messageKey = "login.error.bad.credentials";
                else if (msg.contains("disabled")) messageKey = "login.error.user.disabled";
                else if (msg.contains("expired")) messageKey = "login.error.account.expired";
                else if (msg.contains("locked")) messageKey = "login.error.account.locked";
            }
            model.addAttribute("errorMessage", messageSource.getMessage(messageKey, null, locale));
        }
        return "pages/login.html";
    }

    // Helper parse thời gian
    private Instant parseInstant(String val, Instant defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return val.matches("\\d+") ? Instant.ofEpochMilli(Long.parseLong(val)) : Instant.parse(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}