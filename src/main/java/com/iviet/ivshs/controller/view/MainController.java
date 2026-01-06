package com.iviet.ivshs.controller.view;

import com.iviet.ivshs.constant.I18nMessageConstant;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.*;
import com.iviet.ivshs.util.I18nMessageUtil;
import com.iviet.ivshs.util.LocalContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
public class MainController {

    private final FloorService floorService;
    private final RoomService roomService;
    private final LightService lightService;
    private final DeviceControlService deviceControlService;
    private final TemperatureValueService temperatureValueService;
    private final PowerConsumptionValueService powerConsumptionValueService;
    private final I18nMessageUtil i18nMessageUtil;

    private static final String LANGUAGE_ATTR = "currentlanguage";

    @GetMapping("/")
    public String main() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("messagewelcome", i18nMessageUtil.getMessage(I18nMessageConstant.MSG_WELCOME));
        model.addAttribute(LANGUAGE_ATTR, LocalContextUtil.getCurrentLocale());

        List<FloorDto> floors = floorService.getList(0, 1000).content();
        
        Map<FloorDto, List<RoomDto>> floorRoomsMap = floors.stream()
                .collect(Collectors.toMap(
                        f -> f,
                        f -> roomService.getListByFloor(f.id(), 0, 1000).content(),
                        (oldV, newV) -> oldV,
                        LinkedHashMap::new
                ));

        Instant endedAt = Instant.now();
        Instant startedAt = endedAt.minus(15, ChronoUnit.MINUTES);

        List<RoomDto> allRooms = floorRoomsMap.values().stream()
                .flatMap(List::stream)
                .toList();

        Map<Long, Long> roomGatewayCountMap = new HashMap<>();
        Map<Long, Double> roomCurrentTempMap = new HashMap<>();
        Map<Long, Double> roomCurrentPowerMap = new HashMap<>();

        allRooms.forEach(room -> {
            Long rId = room.id();
            roomGatewayCountMap.put(rId, deviceControlService.countByRoomId(rId));

            var tempHist = temperatureValueService.getAverageTemperatureByRoom(rId, startedAt, endedAt);
            roomCurrentTempMap.put(rId, tempHist.isEmpty() ? 0.0 : tempHist.get(tempHist.size() - 1).avgTempC());

            var powerHist = powerConsumptionValueService.getSumPowerConsumptionByRoom(rId, startedAt, endedAt);
            roomCurrentPowerMap.put(rId, powerHist.isEmpty() ? 0.0 : powerHist.get(powerHist.size() - 1).getSumWatt());
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
            RoomDto room = roomService.getById(roomId);
            model.addAttribute("room", room);
            model.addAttribute("pageTitle", room.name());

            Instant now = Instant.now();
            Instant defaultStart = now.minus(15, ChronoUnit.MINUTES);

            Instant chartStart = parseInstant(startedAtStr, defaultStart);
            Instant chartEnd = parseInstant(endedAtStr, now);

            if (chartStart.isAfter(chartEnd)) {
                Instant temp = chartStart; chartStart = chartEnd; chartEnd = temp;
            }

            var tempChartData = temperatureValueService.getAverageTemperatureByRoom(roomId, chartStart, chartEnd);
            var powerChartData = powerConsumptionValueService.getSumPowerConsumptionByRoom(roomId, chartStart, chartEnd);
            var lights = lightService.getListByRoomId(roomId, 0, 1000).content();

            model.addAttribute("currentTemp", tempChartData.isEmpty() ? 0.0 : tempChartData.get(tempChartData.size() - 1).avgTempC());
            model.addAttribute("currentPower", powerChartData.isEmpty() ? 0.0 : powerChartData.get(powerChartData.size() - 1).getSumWatt());
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
            Object exception = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            String messageKey = I18nMessageConstant.LOGIN_ERROR_UNKNOWN;

            if (exception instanceof AuthenticationException authEx) {
                String msg = authEx.getMessage();
                if (msg != null) {
                    if (msg.contains("Bad credentials")) messageKey = I18nMessageConstant.LOGIN_ERROR_BAD_CREDENTIALS;
                    else if (msg.contains("disabled")) messageKey = I18nMessageConstant.LOGIN_ERROR_USER_DISABLED;
                    else if (msg.contains("expired")) messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_EXPIRED;
                    else if (msg.contains("locked")) messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_LOCKED;
                    else if (msg.contains("not found")) messageKey = I18nMessageConstant.LOGIN_ERROR_USER_NOT_FOUND;
                    else if (msg.contains("Client type is not USER")) messageKey = I18nMessageConstant.LOGIN_ERROR_INVALID_CLIENT_TYPE;
                }
            }
            model.addAttribute("errorMessage", i18nMessageUtil.getMessage(messageKey));
        }
        return "pages/login.html";
    }

    private Instant parseInstant(String val, Instant defaultVal) {
        if (val == null || val.isBlank()) return defaultVal;
        try {
            return val.matches("\\d+") ? Instant.ofEpochMilli(Long.parseLong(val)) : Instant.parse(val);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
