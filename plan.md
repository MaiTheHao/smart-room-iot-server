# Báo cáo phân tích vấn đề phân quyền Alert
## Giải pháp 3 - Hybrid Permission Model (Khuyến nghị)

---

## 1. Bối cảnh vấn đề

### 1.1 Mô hình hiện tại (RBAC + Group Scope)

Hệ thống sử dụng kết hợp RBAC (Role-Based Access Control) và phạm vi dữ liệu (Group Scope):

```
User → multiple Groups
    ↓
Each Group → Role
    ↓
Each Role → Set of Permissions

LOGIN → Flatten all Permissions from all Groups
    ↓
UserPrincipal.authorities = {F_ACCESS_ALERT, F_HANDLE_ALERT, ...}
```

**Đặc điểm:**
- Kiểm tra quyền rất nhanh: O(1) lookup trên Set
- Mất thông tin: Permission này đến từ Group nào?

### 1.2 Rủi ro bảo mật: Cross-group Privilege Escalation

**Tình huống:**
```
User belongs to:
├─ Group A (Room A - ReadOnly)
│  └─ Role: Viewer
│     └─ Permissions: {F_ACCESS_ALERT}
│
└─ Group B (Room B - ReadWrite)
   └─ Role: Operator
      └─ Permissions: {F_ACCESS_ALERT, F_HANDLE_ALERT}

↓ (During Login)

UserPrincipal.authorities = {F_ACCESS_ALERT, F_HANDLE_ALERT}
```

**Cơ chế lỗi:**

```
Khi xử lý Alert của Group A:

1. checkHandleAccess(alertId):
   - Check: hasAnyAuthority('F_HANDLE_ALERT')
     → TRUE (từ Group B)
   
   - Check: isClientInAlertGroups(userId, alertGroupIds)
     → TRUE (User thuộc Group A)
   
   - Result: ✅ ALLOWED (sai!)

2. Người dùng có thể HANDLE Alert của Phòng A
   dù Group A chỉ có quyền READ
```

**Nguyên nhân gốc:** Permission được flatten mà **không lưu mapping Group → Permission**, dẫn tới kiểm tra quyền độc lập với Group context.

---

## 2. Phân tích ba phương án

### 2.1 Phương án 1: Xóa F_HANDLE_ALERT

**Ý tưởng:** Nếu Read = Handle, loại bỏ F_HANDLE_ALERT.

**Ưu điểm:**
- Triển khai ngay lập tức (1-2 giờ)
- Không thay đổi cơ sở hạ tầng bảo mật

**Nhược điểm:**
- ❌ Không giải quyết nguyên nhân gốc
- ❌ Business hiếm khi cam kết "Read = Handle mãi mãi"
- ❌ Nếu sau này cần thêm Read-only Viewer hoặc Supervisor role, phải viết lại từ đầu
- ❌ Mất audit trail (không biết permission đến từ group nào)

**Kết luận:** Giải pháp tạm thời, không bền vững.

---

### 2.2 Phương án 2: Redesign Permission theo Group (Full RBAC)

**Ý tưởng:** Thay đổi UserPrincipal từ Set-based sang Map-based.

```java
// Before
UserPrincipal {
    userId: Long
    authorities: Set<String> = {F_ACCESS_ALERT, F_HANDLE_ALERT}
}

// After
UserPrincipal {
    userId: Long
    groupPermissions: Map<GroupId, Set<Permission>> = {
        GroupA → {F_ACCESS_ALERT},
        GroupB → {F_ACCESS_ALERT, F_HANDLE_ALERT}
    }
}
```

**Ưu điểm:**
- ✅ Giải quyết triệt để Cross-group Privilege Escalation
- ✅ RBAC đúng chuẩn, dễ mở rộng
- ✅ Audit trail rõ ràng

**Nhược điểm:**
- ❌ **Breaking change:** @PreAuthorize không còn hoạt động
- ❌ Phải thay đổi toàn bộ Spring Security expression
- ❌ Cần custom SecurityExpressionRoot (tăng độ phức tạp)
- ❌ Performance: O(1) → O(groups) lookup
- ❌ Refactor lớn: 30-50% codebase bị touch
- ❌ Chi phí: 2-3 ngày, risky, dễ regression

**Kết luận:** Đúng về kỹ thuật nhưng **quá phức tạp cho monolith hiện tại**.

---

### 2.3 Phương án 3: Hybrid Permission Model (KHUYẾN NGHỊ) ⭐

**Ý tưởng:** Giữ flattened authorities để Spring Security hoạt động bình thường, nhưng **thêm Group-Permission mapping** để kiểm tra context.

```java
// UserPrincipal - KHÔNG thay đổi cấu trúc
UserPrincipal {
    userId: Long
    
    // Giữ nguyên (compatibility)
    authorities: Set<String> = {F_ACCESS_ALERT, F_HANDLE_ALERT}
    
    // THÊM: Mapping permission → groups (mới)
    permissionToGroupIds: Map<String, Set<Long>> = {
        "F_ACCESS_ALERT" → {GroupA, GroupB},
        "F_HANDLE_ALERT" → {GroupB}
    }
}
```

**Logic kiểm tra quyền mới:**

```java
// Cũ (lỗi)
if (hasAuthority('F_HANDLE_ALERT') && isClientInAlertGroups(...)) {
    // ✅ Nhưng không check nếu F_HANDLE_ALERT áp dụng cho group này
    // ❌ Cross-group bypass có thể xảy ra
}

// Mới (đúng)
if (principal.hasGroupPermission('F_HANDLE_ALERT', alertGroupId)) {
    // ✅ Check: F_HANDLE_ALERT có áp dụng cho alertGroupId không?
    // ✅ An toàn, không cross-group
}
```

**Ưu điểm:**
- ✅ **Giải quyết gốc rễ** Cross-group Privilege Escalation
- ✅ **Không breaking change:** @PreAuthorize vẫn hoạt động
- ✅ **Spring Security convention:** Dễ maintain, dễ train team
- ✅ **Incremental:** Có thể refactor từng API endpoint một
- ✅ **Audit trail:** Biết permission áp dụng cho group nào
- ✅ **Performance:** Vẫn O(1) lookup
- ✅ **Simple:** Logic rõ ràng, không custom SecurityExpressionRoot

**Nhược điểm:**
- ⚠️ Thêm 1 field vào UserPrincipal
- ⚠️ PrincipalDataExtractor phải query thêm dữ liệu (nhưng query đó rất đơn giản)
- ⚠️ Session size tăng nhẹ (với 10 groups, +0.5KB/session)

**Timeline:** 4-6 giờ, có thể làm incremental

**Kết luận:** **Cân bằng tốt nhất giữa Security, Simplicity, Pragmatism cho monolith.**

---

## 3. Giải pháp 3 - Chi tiết triển khai

### 3.1 Bước 1: Thay đổi UserPrincipal

**File:** `src/main/java/com/iviet/ivshs/security/UserPrincipal.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long userId;
    private String username;
    private String password;
    
    // Existing - giữ nguyên
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;
    
    // NEW: Group-Permission mapping
    // Format: "F_HANDLE_ALERT" → {groupId1, groupId2}
    private Map<String, Set<Long>> permissionToGroupIds;
    
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    /**
     * Kiểm tra người dùng có một permission cụ thể trong một group cụ thể không.
     * 
     * @param permission Tên permission (VD: "F_HANDLE_ALERT")
     * @param groupId ID của group
     * @return true nếu permission áp dụng cho group này
     */
    public boolean hasGroupPermission(String permission, Long groupId) {
        if (permissionToGroupIds == null) {
            return false;
        }
        
        Set<Long> allowedGroups = permissionToGroupIds.get(permission);
        return allowedGroups != null && allowedGroups.contains(groupId);
    }
    
    /**
     * Kiểm tra người dùng có một permission cụ thể trong BẤT KỲ group nào không.
     * Sử dụng cho các resource không phụ thuộc group (VD: System Settings).
     * 
     * @param permission Tên permission
     * @return true nếu permission có trong bất kỳ group nào
     */
    public boolean hasAnyGroupPermission(String permission) {
        if (permissionToGroupIds == null) {
            return false;
        }
        return permissionToGroupIds.containsKey(permission);
    }
    
    /**
     * Lấy danh sách tất cả groups mà người dùng có một permission cụ thể.
     * Sử dụng cho filter data (VD: chỉ lấy Alerts của groups được phép handle).
     * 
     * @param permission Tên permission
     * @return Set<Long> groupIds
     */
    public Set<Long> getGroupsWithPermission(String permission) {
        if (permissionToGroupIds == null) {
            return Collections.emptySet();
        }
        return permissionToGroupIds.getOrDefault(permission, Collections.emptySet());
    }
}
```

---

### 3.2 Bước 2: Update PrincipalDataExtractor

**File:** `src/main/java/com/iviet/ivshs/security/UserDetailsServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final SysGroupRepository groupRepository;  // Cần inject
    private final RoleRepository roleRepository;       // Cần inject
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsernameAndDeletedFalse(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return buildUserPrincipal(user);
    }
    
    /**
     * Xây dựng UserPrincipal với Group-Permission mapping.
     */
    private UserPrincipal buildUserPrincipal(SysUser user) {
        // 1. Lấy tất cả groups của user
        List<SysGroup> userGroups = groupRepository.findByUsersAndDeletedFalse(user);
        
        // 2. Tích lũy authorities (flatten - giữ behavior hiện tại)
        Set<String> authorities = new HashSet<>();
        
        // 3. Xây dựng permissionToGroupIds mapping (mới)
        Map<String, Set<Long>> permissionToGroupIds = new HashMap<>();
        
        for (SysGroup group : userGroups) {
            // Lấy role của user trong group này
            SysRole role = group.getRole(); // Hoặc dùng junction table nếu user-group-role là many-to-many
            
            if (role != null && !role.isDeleted()) {
                // Lấy permissions của role
                Set<SysPermission> permissions = role.getPermissions();
                
                for (SysPermission permission : permissions) {
                    String permissionName = permission.getName(); // VD: "F_HANDLE_ALERT"
                    
                    // Thêm vào flattened authorities
                    authorities.add(permissionName);
                    
                    // Thêm vào mapping: permission → {groupId1, groupId2, ...}
                    permissionToGroupIds
                        .computeIfAbsent(permissionName, k -> new HashSet<>())
                        .add(group.getId());
                }
            }
        }
        
        // 4. Build UserPrincipal
        return UserPrincipal.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(
                authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
            )
            .permissionToGroupIds(permissionToGroupIds)
            .accountNonExpired(true)
            .accountNonLocked(!user.isLocked())
            .credentialsNonExpired(true)
            .enabled(!user.isDeleted())
            .build();
    }
}
```

**Query optimization (nếu cần):**

```java
// Nếu userGroups query hiện tại là N+1:
@Query("""
    SELECT g FROM SysGroup g
    WHERE :user MEMBER OF g.users
    AND g.deleted = false
    AND g.role.deleted = false
""")
List<SysGroup> findByUsersAndDeletedFalse(@Param("user") SysUser user);

// Nếu permission fetch strategy là LAZY:
@Query("""
    SELECT DISTINCT g FROM SysGroup g
    LEFT JOIN FETCH g.role r
    LEFT JOIN FETCH r.permissions p
    WHERE :user MEMBER OF g.users
    AND g.deleted = false
""")
List<SysGroup> findByUsersWithRoleAndPermissions(@Param("user") SysUser user);
```

---

### 3.3 Bước 3: Update Authorization Logic

**Mẫu cập nhật:** AlertInstanceServiceImpl.checkHandleAccess()

#### Cách 1: Method-level check (Recommended - Simple)

**File:** `src/main/java/com/iviet/ivshs/service/alert/impl/AlertInstanceServiceImpl.java`

```java
@Service
@RequiredArgsConstructor
public class AlertInstanceServiceImpl implements AlertInstanceService {
    
    private final AlertInstanceRepository alertInstanceRepository;
    private final SysGroupRepository groupRepository;
    
    /**
     * Kiểm tra quyền xử lý Alert (Handle) của người dùng.
     * 
     * Điều kiện cần:
     * 1. User phải có quyền F_HANDLE_ALERT trong GROUP được liên kết với Alert
     *    (Không phải bất kỳ group nào)
     * 
     * @param userId User ID
     * @param alertInstanceId Alert Instance ID
     * @throws AccessDeniedException nếu không có quyền
     */
    public void checkHandleAccess(Long userId, Long alertInstanceId) {
        
        // 1. Lấy Alert Instance
        AlertInstance alertInstance = alertInstanceRepository
            .findById(alertInstanceId)
            .orElseThrow(() -> new NotFoundException("Alert not found"));
        
        // 2. Lấy Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        
        // 3. Lấy tất cả groups được liên kết với Alert này
        Set<Long> alertGroupIds = alertInstance.getGroups()
            .stream()
            .map(SysGroup::getId)
            .collect(Collectors.toSet());
        
        if (alertGroupIds.isEmpty()) {
            throw new AccessDeniedException("Alert has no assigned groups");
        }
        
        // 4. Kiểm tra: User có F_HANDLE_ALERT trong BẤT KỲ group nào của Alert không?
        boolean hasHandlePermissionInAlertGroups = alertGroupIds.stream()
            .anyMatch(groupId -> principal.hasGroupPermission("F_HANDLE_ALERT", groupId));
        
        if (!hasHandlePermissionInAlertGroups) {
            throw new AccessDeniedException(
                "User does not have F_HANDLE_ALERT permission in any alert group"
            );
        }
    }
    
    /**
     * Kiểm tra quyền xem Alert (Access) của người dùng.
     * Tương tự nhưng với permission F_ACCESS_ALERT.
     */
    public void checkAccessAccess(Long userId, Long alertInstanceId) {
        AlertInstance alertInstance = alertInstanceRepository
            .findById(alertInstanceId)
            .orElseThrow(() -> new NotFoundException("Alert not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        
        Set<Long> alertGroupIds = alertInstance.getGroups()
            .stream()
            .map(SysGroup::getId)
            .collect(Collectors.toSet());
        
        boolean hasAccessPermissionInAlertGroups = alertGroupIds.stream()
            .anyMatch(groupId -> principal.hasGroupPermission("F_ACCESS_ALERT", groupId));
        
        if (!hasAccessPermissionInAlertGroups) {
            throw new AccessDeniedException(
                "User does not have F_ACCESS_ALERT permission in any alert group"
            );
        }
    }
}
```

#### Cách 2: Annotation-based check (Khi ready để refactor)

Sau khi method-level checks hoạt động ổn định, có thể tạo custom annotation:

```java
// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireGroupPermission {
    String permission();
    String groupIdParameterName() default "groupId";
}

// Custom advice
@Component
@Aspect
public class GroupPermissionAdvice {
    
    @Around("@annotation(requireGroupPermission)")
    public Object check(ProceedingJoinPoint joinPoint, 
                        RequireGroupPermission requireGroupPermission) 
            throws Throwable {
        
        UserPrincipal principal = getPrincipalFromContext();
        
        // Extract groupId từ method parameter
        long groupId = extractGroupId(joinPoint, requireGroupPermission);
        
        if (!principal.hasGroupPermission(requireGroupPermission.permission(), groupId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        return joinPoint.proceed();
    }
}

// Usage trong API
@PostMapping("/{alertInstanceId}/acknowledge")
@RequireGroupPermission(permission = "F_HANDLE_ALERT", groupIdParameterName = "alertGroupId")
public ResponseEntity<AlertInstanceDto> acknowledgeAlert(
    @PathVariable Long alertInstanceId,
    @RequestParam Long alertGroupId) {
    
    // Method body - quyền đã được check
    ...
}
```

---

### 3.4 Bước 4: Update AlertController

**File:** `src/main/java/com/iviet/ivshs/controller/api/v1/AlertController.java`

```java
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {
    
    private final AlertInstanceService alertInstanceService;
    
    /**
     * Acknowledge (xác nhận) Alert.
     * 
     * Kiểm tra:
     * - User phải có F_HANDLE_ALERT trong GROUP của Alert
     */
    @PostMapping("/{alertInstanceId}/acknowledge")
    public ResponseEntity<AlertInstanceDto> acknowledgeAlert(
        @PathVariable Long alertInstanceId,
        @RequestBody AcknowledgeAlertRequest request) {
        
        // Kiểm tra quyền trong service
        alertInstanceService.checkHandleAccess(getCurrentUserId(), alertInstanceId);
        
        AlertInstance alert = alertInstanceService.acknowledgeAlert(alertInstanceId, request);
        return ResponseEntity.ok(mapToDto(alert));
    }
    
    /**
     * Resolve (giải quyết) Alert.
     */
    @PostMapping("/{alertInstanceId}/resolve")
    public ResponseEntity<AlertInstanceDto> resolveAlert(
        @PathVariable Long alertInstanceId,
        @RequestBody ResolveAlertRequest request) {
        
        alertInstanceService.checkHandleAccess(getCurrentUserId(), alertInstanceId);
        
        AlertInstance alert = alertInstanceService.resolveAlert(alertInstanceId, request);
        return ResponseEntity.ok(mapToDto(alert));
    }
    
    /**
     * List Alerts (lấy danh sách Alerts người dùng được phép xem).
     * 
     * Optimization: Chỉ lấy Alerts của Groups mà user có F_ACCESS_ALERT.
     */
    @GetMapping
    public ResponseEntity<Page<AlertInstanceDto>> listAlerts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        
        UserPrincipal principal = (UserPrincipal) 
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Optimization: Lấy groups mà user có F_ACCESS_ALERT
        Set<Long> accessibleGroupIds = principal
            .getGroupsWithPermission("F_ACCESS_ALERT");
        
        if (accessibleGroupIds.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }
        
        // Chỉ query Alerts của groups mà user có quyền
        Page<AlertInstance> alerts = alertInstanceService
            .listAlertsByGroupIds(accessibleGroupIds, page, size);
        
        return ResponseEntity.ok(alerts.map(this::mapToDto));
    }
    
    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) 
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
```

---

### 3.5 Bước 5: Unit Tests

**File:** `src/test/java/com/iviet/ivshs/service/alert/AlertInstanceServiceImplTest.java`

```java
@ExtendWith(MockitoExtension.class)
class AlertInstanceServiceImplTest {
    
    @InjectMocks
    private AlertInstanceServiceImpl alertInstanceService;
    
    @Mock
    private AlertInstanceRepository alertInstanceRepository;
    
    @Mock
    private SysGroupRepository groupRepository;
    
    @Test
    @DisplayName("Should allow handle alert when user has F_HANDLE_ALERT in alert group")
    void testCheckHandleAccess_Success() {
        // Given
        long userId = 1L;
        long alertId = 100L;
        long groupA = 10L;
        
        UserPrincipal principal = UserPrincipal.builder()
            .userId(userId)
            .username("testuser")
            .authorities(List.of(
                new SimpleGrantedAuthority("F_ACCESS_ALERT"),
                new SimpleGrantedAuthority("F_HANDLE_ALERT")
            ))
            .permissionToGroupIds(Map.of(
                "F_ACCESS_ALERT", Set.of(groupA),
                "F_HANDLE_ALERT", Set.of(groupA)  // ✅ User có handle trong groupA
            ))
            .enabled(true)
            .build();
        
        AlertInstance alert = AlertInstance.builder()
            .id(alertId)
            .groups(Set.of(
                SysGroup.builder().id(groupA).build()
            ))
            .build();
        
        when(alertInstanceRepository.findById(alertId))
            .thenReturn(Optional.of(alert));
        
        // When
        assertDoesNotThrow(() -> {
            testCheckHandleAccessWithPrincipal(alertInstanceService, principal, userId, alertId);
        });
    }
    
    @Test
    @DisplayName("Should deny handle alert when user lacks F_HANDLE_ALERT in alert group")
    void testCheckHandleAccess_DenyNotInGroup() {
        // Given
        long userId = 1L;
        long alertId = 100L;
        long groupA = 10L;
        long groupB = 20L;
        
        UserPrincipal principal = UserPrincipal.builder()
            .userId(userId)
            .username("testuser")
            .authorities(List.of(
                new SimpleGrantedAuthority("F_ACCESS_ALERT"),
                new SimpleGrantedAuthority("F_HANDLE_ALERT")
            ))
            .permissionToGroupIds(Map.of(
                "F_ACCESS_ALERT", Set.of(groupB),
                "F_HANDLE_ALERT", Set.of(groupB)  // ❌ User có handle ở groupB, không ở groupA
            ))
            .enabled(true)
            .build();
        
        AlertInstance alert = AlertInstance.builder()
            .id(alertId)
            .groups(Set.of(
                SysGroup.builder().id(groupA).build()  // Alert ở groupA
            ))
            .build();
        
        when(alertInstanceRepository.findById(alertId))
            .thenReturn(Optional.of(alert));
        
        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            testCheckHandleAccessWithPrincipal(alertInstanceService, principal, userId, alertId);
        });
    }
    
    @Test
    @DisplayName("Should deny handle alert even if user has flattened F_HANDLE_ALERT but not in alert group")
    void testCheckHandleAccess_CrossGroupPrivilegeEscalation() {
        // Given: Cross-group scenario
        long userId = 1L;
        long alertId = 100L;
        long groupReadOnly = 10L;  // ReadOnly
        long groupReadWrite = 20L;  // ReadWrite
        
        UserPrincipal principal = UserPrincipal.builder()
            .userId(userId)
            .username("testuser")
            .authorities(List.of(
                new SimpleGrantedAuthority("F_ACCESS_ALERT"),
                new SimpleGrantedAuthority("F_HANDLE_ALERT")  // ❌ Flattened from groupReadWrite
            ))
            .permissionToGroupIds(Map.of(
                "F_ACCESS_ALERT", Set.of(groupReadOnly, groupReadWrite),
                "F_HANDLE_ALERT", Set.of(groupReadWrite)  // ✅ Mapping rõ ràng: chỉ ở groupReadWrite
            ))
            .enabled(true)
            .build();
        
        AlertInstance alert = AlertInstance.builder()
            .id(alertId)
            .groups(Set.of(
                SysGroup.builder().id(groupReadOnly).build()  // Alert ở groupReadOnly
            ))
            .build();
        
        when(alertInstanceRepository.findById(alertId))
            .thenReturn(Optional.of(alert));
        
        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            testCheckHandleAccessWithPrincipal(alertInstanceService, principal, userId, alertId);
        }, "Should deny cross-group privilege escalation");
    }
}
```

---

## 4. Triển khai Incremental

### Giai đoạn 1 (Sprint 1): Cơ sở hạ tầng
**Timeline:** 2-3 giờ

- [ ] Thêm `permissionToGroupIds` vào UserPrincipal
- [ ] Thêm helper methods: `hasGroupPermission()`, `getGroupsWithPermission()`
- [ ] Update UserDetailsServiceImpl để populate mapping
- [ ] Unit tests cho UserPrincipal

**Output:** UserPrincipal có đầy đủ thông tin, nhưng chưa được sử dụng trong logic kiểm tra quyền.

### Giai đoạn 2 (Sprint 2): Cập nhật Alert Module
**Timeline:** 2-3 giờ

- [ ] Update AlertInstanceServiceImpl: `checkHandleAccess()`, `checkAccessAccess()`
- [ ] Update AlertController endpoints
- [ ] Unit tests + Integration tests
- [ ] Kiểm tra regression

**Output:** Alert authorization 100% an toàn từ Cross-group Privilege Escalation.

### Giai đoạn 3 (Sprint 3+): Mở rộng sang modules khác
**Timeline:** Tùy số modules

- [ ] Device authorization (nếu có)
- [ ] Report authorization
- [ ] Rule authorization
- [ ] etc.

---

## 5. Trade-off và So sánh

| Tiêu chí | Phương án 1 | Phương án 2 | Phương án 3 |
|---------|-----------|-----------|-----------|
| **Triển khai** | ⚡ 1-2h | 🔴 2-3 ngày | 🟡 4-6h |
| **Breaking change** | ❌ Behavior | 🔴 API | ✅ Không |
| **Giải quyết gốc rễ** | 🔴 Không | ✅ Có | ✅ Có |
| **Security** | 🟡 Tạm thời | ✅ Tối ưu | ✅ Đủ |
| **Audit trail** | ❌ Mất | ✅ Có | ✅ Có |
| **Spring Security** | ✅ Không thay | 🔴 Thay đổi | ✅ Bình thường |
| **Incremental** | ✅ Không cần | ❌ All-or-nothing | ✅ Có thể |
| **Maintain** | ✅ Đơn giản | 🔴 Phức tạp | ✅ Đơn giản |
| **Team learn** | ✅ Nhanh | 🔴 Chậm | ✅ Nhanh |

---

## 6. Migration Checklist

### Trước triển khai
- [ ] Database backup
- [ ] Staging environment ready
- [ ] Load test baseline ready
- [ ] Monitoring alerts configured

### Triển khai Giai đoạn 1
- [ ] Code review UserPrincipal changes
- [ ] Unit test pass 100%
- [ ] Deploy to staging
- [ ] Smoke test: Login, check principal in logs

### Triển khai Giai đoạn 2
- [ ] Code review AlertInstanceServiceImpl
- [ ] Integration test pass 100%
- [ ] Deploy to staging
- [ ] Functional test: Alert Handle scenarios
- [ ] Cross-group privilege escalation test
- [ ] Deploy to production (canary 10% → 50% → 100%)

### Post-deployment
- [ ] Monitor error logs (AccessDeniedException)
- [ ] Verify no false negatives (users denied legitimate access)
- [ ] Performance metrics (session size, authorization latency)
- [ ] Update documentation

---

## 7. Performance Considerations

### Memory impact (per session)

**Giả sử:**
- Average user: 5 groups
- Average role: 10 permissions (nhưng cùng permission có thể ở nhiều groups)
- Unique permissions: ~30 system-wide

**Calculation:**

```
permissionToGroupIds = Map<String, Set<Long>>

Per entry: 
- String key (~20 bytes) = 20B
- Set<Long> với 5 group IDs = 8B * 5 = 40B
- Overhead (HashMap entry) = ~40B

~100B per permission
~30 permissions × 100B = ~3KB per user

Total session size increase: 3-5KB (acceptable)
```

### Query performance

**Current:** 
```
PrincipalDataExtractor:
  - SELECT user ... WHERE username = ? (1 query)
  - SELECT group, role, permission via relationships (N+1 risk)
```

**With optimization:**
```
SELECT DISTINCT g FROM SysGroup g
LEFT JOIN FETCH g.role r
LEFT JOIN FETCH r.permissions
WHERE :user MEMBER OF g.users AND g.deleted = false

- 1 query with eager fetch
- No N+1, performance: 5-10ms vs 50-100ms
```

### Authorization check performance

**Cũ:**
```
hasAuthority('F_HANDLE_ALERT')           // O(1) - Set lookup
isClientInAlertGroups(userId, groupIds)  // O(n) - DB query or set intersection
```

**Mới:**
```
hasGroupPermission('F_HANDLE_ALERT', groupId)  
// O(1) - Map lookup + Set lookup
// Faster than DB query, and already in memory
```

---

## 8. Rollback Plan

Nếu cần rollback sau triển khai:

### Triển khai 1 tuần
```
1. Alert: Remove permissionToGroupIds từ UserPrincipal response/logs
2. Downgrade: Revert code (Git revert)
3. Restart: Application restart (session clear)
4. Verify: Functional test
```

### Timeline: ~15 phút (low risk)

---

## 9. Tài liệu hỗ trợ

### Cần update
- [ ] README: Cập nhật Architecture Decision Record (ADR)
- [ ] API docs: Explain permission model change (nếu API response thay đổi)
- [ ] Runbook: Troubleshooting authorization issues
- [ ] Test guide: How to test cross-group privilege escalation scenarios

### Recommendation
```markdown
# ADR: Hybrid Permission Model for Alert Authorization

## Decision
Implement Group-aware permission checking while maintaining Spring Security compatibility.

## Rationale
- Fast to implement and incremental
- Secure against cross-group privilege escalation
- No breaking changes to existing endpoints
- Educational for team: simple, clear model

## Implementation
1. Add permissionToGroupIds to UserPrincipal
2. Update authorization logic to check group context
3. Incremental rollout: Alert → Device → Report → Rules
```

---

## 10. Kết luận

### Nguyên nhân lỗi
Permission flatten + không lưu mapping Group → Permission = Cross-group Privilege Escalation.

### Giải pháp khuyến nghị
**Phương án 3 - Hybrid Permission Model**

Lý do:
1. ✅ Giải quyết tận gốc vấn đề
2. ✅ Không breaking change
3. ✅ Incremental triển khai
4. ✅ Dễ maintain, dễ hiểu
5. ✅ Phù hợp với monolith hiện tại
6. ✅ Có thể scale lên microservices sau

### Timeline
**4-6 giờ** (ít hơn 1 sprint)

### Rủi ro
**Thấp** - Chỉ thêm dữ liệu, không thay đổi cấu trúc hiện tại

### Next steps
1. Review báo cáo này với Architecture team
2. Approve phương án
3. Bắt đầu Sprint 1: Infrastructure setup
4. Sprint 2: Alert module update
5. Sprint 3+: Mở rộng sang modules khác