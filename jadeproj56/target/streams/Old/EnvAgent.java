package Old;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EnvAgent extends Agent {
    AID speleologist;
    Cave cave;


    private static final long serialVersionUID = 1L;
    public static final String NAME = "ENV";

    protected void setup() {
        cave = new Cave();
        cave.open();
        Helper.registerYellow(Helper.TYPE, NAME, this);
        addBehaviour(Helper.getWaitBehaviour(this, Speleologist.NAME, new CallBehaviour()));
    }

    protected void takeDown() {
        Helper.deregister(this);
        System.out.println("agent " + getAID().getName() + " terminating.");
    }


    private class ThinkBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        public void action() {
            ACLMessage cfp = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
            ACLMessage request = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            if (cfp != null) {
                cave.eatAction(cfp.getContent());
                if (cfp.getContent().contains(KB.CLIMB)) {
                    String reply = cave.gold ? KB.END + ", failure" : KB.END + ", success";
                    Helper.sendReply(cfp, ACLMessage.ACCEPT_PROPOSAL, reply, myAgent);
                } else {
                    Helper.sendReply(cfp, ACLMessage.ACCEPT_PROPOSAL, "Ok", myAgent);
                }
            }
            if (request != null) {

                String pred = KB.roomToPredicate(cave.getAgentRoom(), cave.time, cave);

                Helper.sendReply(request, ACLMessage.INFORM, pred, myAgent);
            } else {
                block();
            }
        }
    }

    private class CallBehaviour extends OneShotBehaviour {
        private static final long serialVersionUID = 1L;

        public void action() {
            speleologist = Helper.getFromYellow(myAgent, Helper.TYPE, Speleologist.NAME);
            Helper.sendMsg(speleologist, "Ok", ACLMessage.ACCEPT_PROPOSAL, myAgent);
            addBehaviour(new ThinkBehaviour());
        }
    }
}
