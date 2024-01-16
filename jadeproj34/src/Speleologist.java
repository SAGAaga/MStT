import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Speleologist extends Agent {
    private static final long serialVersionUID = 1L;
    public static final String NAME = "SP";
    private AID navigator;
    private AID environment;

    protected void setup() {
        Helper.registerYellow(Helper.TYPE, NAME, this);

        addBehaviour(Helper.getWaitBehaviour(this, EnvAgent.NAME,
                Helper.getWaitBehaviour(this, Navigator.NAME, new InitBehaviour())));
    }

    protected void takeDown() {
        Helper.deregister(this);
        System.out.println("agent " + getAID().getName() + " terminating.");
    }

    private class InitBehaviour extends OneShotBehaviour {
        private static final long serialVersionUID = 1L;

        public void action() {
            navigator = Helper.getFromYellow(myAgent, Helper.TYPE, Navigator.NAME);
            environment = Helper.getFromYellow(myAgent, Helper.TYPE, EnvAgent.NAME);
            addBehaviour(new WaitBehaviour());
        }
    }

    private class WaitBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        public void action() {
            ACLMessage accept = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
            ACLMessage propose = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            ACLMessage inform = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (accept != null) {
                if (accept.getContent().contains(KB.END)) {
                    System.out.printf(accept.getContent());
                    Helper.stopAll();
                }
                String result = "Where am I?";
                Helper.sendReply(accept, ACLMessage.REQUEST, result, myAgent);
            } else if (inform != null) {
                System.out.println(EnvAgent.NAME + ": " + inform.getContent());
                String result = KB.predicateToFeel(inform.getContent());
                System.out.println(NAME + ": " + result);
                Helper.sendMsg(navigator, result, ACLMessage.INFORM, myAgent);
            } else if (propose != null) {
                System.out.println(Navigator.NAME + ": " + propose.getContent());
                String result = KB.wrapAction(propose.getContent());
                Helper.sendMsg(environment, result, ACLMessage.CFP, myAgent);
            } else {
                block();
            }
        }
    }
}