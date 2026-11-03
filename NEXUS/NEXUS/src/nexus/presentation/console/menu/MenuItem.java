package nexus.presentation.console.menu;

import nexus.security.role.Permission;

public record MenuItem(String label, Permission requiredPermission, Runnable action) { }
