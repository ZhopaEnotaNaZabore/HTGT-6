/** Safe offline probe: prints class origins and checks a harmless system-property lookup. */
public final class Log4jAuditProbe {
    public static void main(String[] args) throws Exception {
        System.setProperty("log4j.configurationFile", args[0]);
        System.setProperty("htgt6.audit.marker", "LOOKUP_EXPANDED");
        for (String name : new String[]{"org.apache.logging.log4j.LogManager",
                "org.apache.logging.log4j.core.LoggerContext", "org.apache.logging.log4j.core.lookup.JndiLookup"}) {
            Class<?> type = Class.forName(name, false, Log4jAuditProbe.class.getClassLoader());
            System.out.println(name + " version=" + type.getPackage().getImplementationVersion()
                    + " origin=" + type.getProtectionDomain().getCodeSource().getLocation());
        }
        Class<?> manager = Class.forName("org.apache.logging.log4j.LogManager");
        Object logger = manager.getMethod("getLogger", String.class).invoke(null, "HTGT6.Log4jAudit");
        Class.forName("org.apache.logging.log4j.Logger").getMethod("info", String.class)
                .invoke(logger, "AUDIT_MESSAGE=${sys:htgt6.audit.marker}");
    }
}
