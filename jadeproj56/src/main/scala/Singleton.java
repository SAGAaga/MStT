public class Singleton {
    private final legacy.Navigator nav;
    private final legacy.Speleologist sp;
    private final legacy.EnvAgent env;
    private static Singleton inst;

    private static void init() {
        if (inst == null) {
            inst = new Singleton();
        }
    }

    public static String processNav(String msg) {
        init();
        return inst.nav.process(msg);
    }

    public static String processEnv(String msg) {
        init();
        return inst.env.process(msg);
    }

    public static String processSp(String msg) {
        init();
        return inst.sp.process(msg);
    }

    private Singleton() {
        env = new legacy.EnvAgent();
        nav = new legacy.Navigator();
        sp = new legacy.Speleologist();
    }
}
