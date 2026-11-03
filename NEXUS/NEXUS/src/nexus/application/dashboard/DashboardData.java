package nexus.application.dashboard;

import java.util.Map;

/** Holds key-value metric summary pairs rendered in the role-based dashboard. */
public record DashboardData(String roleName, String welcomeMessage, Map<String, String> metrics) { }
