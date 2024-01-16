package legacy;

import java.util.HashMap;

public class KB {
    public static final String AGENT = "A";
    public static final String BREEZE = "B";
    public static final String GOLD = "G";
    public static final String PIT = "P";
    public static final String STENCH = "S";
    public static final String WAMPUS = "W";
    public static final String SAFE = "F";
    public static final String VISITED = "V";
    public static final String UNKNOWN = "?";

    public static final String PER_ST = "stench";
    public static final String PER_BR = "breeze";
    public static final String PER_GL = "glitter";
    public static final String PER_BU = "bump";
    public static final String PER_SC = "scream";


    public static final String I_HEAR = "I hear scream";

    public static final String I_GOT = "I got bump";

    public static final String I_SEE = "I see glitter";

    public static final String I_FEEL = "I feel breeze";

    public static final String I_SMELL = "I smell stench";
    public static final String NONE = "None";

    public static final String TURN_RIGHT = "Turn(Right)";
    public static final String TURN_LEFT = "Turn(Left)";
    public static final String TURN_AROUND = "Turn(Left), Turn(Left)";
    public static final String FORWARD = "Forward";
    public static final String SHOOT = "Shoot";
    public static final String GRAB = "Grab";
    public static final String CLIMB = "Climb";
    public static final String END = "END";
    public static final String ACTION = "Action";
    public static final String REQUEST = "Request";
    public static final String OK = "OK";
    public static HashMap<String, String> dict = new HashMap<>();

    static {
        dict.put(PER_SC, I_HEAR);
        dict.put(PER_BU, I_GOT);
        dict.put(PER_GL, I_SEE);
        dict.put(PER_BR, I_FEEL);
        dict.put(PER_ST, I_SMELL);

        dict.put(I_SEE, GOLD);
        dict.put(I_FEEL, BREEZE);
        dict.put(I_SMELL, STENCH);

        dict.put(GOLD, PER_GL);
        dict.put(BREEZE, PER_BR);
        dict.put(STENCH, PER_ST);
    }

    public static String translate(String word) {
        return dict.getOrDefault(word, NONE);
    }

    public static String wrapAction(String s) {
        return "Action(" + s + ")";
    }

    public static String unwrapAction(String s) {
        return s.replace("Action(", "").replace(")", "");
    }
    public static String roomToPredicate(String room, long time, Cave cave) {
        StringBuilder stringBuilder = new StringBuilder();
        Predicate predicate = new Predicate();
        predicate.stench = room.contains(KB.STENCH);
        predicate.breeze = room.contains(KB.BREEZE);
        predicate.glitter = room.contains(KB.GOLD);
        predicate.scream = cave.scream;
        cave.scream = false;
        stringBuilder.append(predicate);
        stringBuilder.append(time);
        return stringBuilder.toString();
    }
    public static String predicateToFeel(String pred) {
        pred = pred.replaceAll("] .*", "").replaceAll("[\\[\\s]", "");
        String[] parse = pred.split(",");
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        for (String s : parse) {
            temp = translate(s);
            if (temp.equals(NONE)) continue;
            stringBuilder.append(temp);
            stringBuilder.append(". ");
        }
        return stringBuilder.toString();
    }

    public static String feelsToRoom(String feels) {
        StringBuilder sb = new StringBuilder();
        for (String s : feels.split("\\. ")) {
            sb.append(translate(s).equals(NONE) ? "" : translate(s));
        }
        return sb.toString();
    }

    //1 if OK
    public static boolean isOK(String s) {
        return s.contains(KB.VISITED) || s.contains(KB.SAFE);
    }

    private static class Predicate {
        public boolean scream;
        public boolean bump;
        public boolean glitter;
        public boolean breeze;
        public boolean stench;

        @Override
        public String toString() {
            return "[" + (scream ? KB.PER_SC : KB.NONE) +
                    ", " + (bump ? KB.PER_BU : KB.NONE) +
                    ", " + (glitter ? KB.PER_GL : KB.NONE) +
                    ", " + (breeze ? KB.PER_BR : KB.NONE) +
                    ", " + (stench ? KB.PER_ST : KB.NONE) +
                    "] ";
        }
    }


}
