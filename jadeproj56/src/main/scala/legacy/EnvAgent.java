package legacy;

public class EnvAgent implements Processable {
    Cave cave;

    private static final long serialVersionUID = 1L;
    public static final String NAME = "ENV";

    public EnvAgent() {
        setup();
    }

    protected void setup() {
        cave = new Cave();
        cave.open();
    }

    @Override
    public String process(String msg) {
        if (msg.contains(KB.ACTION)) {
            cave.eatAction(msg);
            if (msg.contains(KB.CLIMB)) {
                return cave.gold ? KB.END + ", failure" : KB.END + ", success";
            } else {
                return KB.OK;
            }
        }
        if (msg.contains(KB.REQUEST)) {
            String pred = KB.roomToPredicate(cave.getAgentRoom(), cave.time, cave);
            return pred;
        }
        return "";
    }
}
