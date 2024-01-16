package Old;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.LinkedList;
import java.util.List;

public class Helper {
    public static final String TYPE = "world";

    private static List<Agent> agents = new LinkedList<>();
    public static void sendMsg(AID receiver, String content, int type, Agent context) {
        //System.out.println(context.getName() + " to " + receiver.getName() + ": " + content);
        ACLMessage state = new ACLMessage(type);
        state.setContent(content);
        state.addReceiver(receiver);
        state.setConversationId(Helper.TYPE);
        context.send(state);
    }

    public static void sendReply(ACLMessage origin, int type, String content, Agent context) {
        sendMsg(origin.getSender(), content, type, context);
    }

    public static Behaviour getWaitBehaviour(Agent context, String name, Behaviour next) {
        return new TickerBehaviour(context, 1000) {
            private static final long serialVersionUID = 1L;

            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(TYPE);
                sd.setName(name);
                template.addServices(sd);
                try {
                    System.out.println(context.getName() + " is waiting for " + name);
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        System.out.println(context.getName() + " is ready with " + name);
                        if (next != null) {
                            myAgent.addBehaviour(next);
                        }
                        myAgent.removeBehaviour(this);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };
    }

    public static void registerYellow(String type, String name, Agent agent) {
        agents.add(agent);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        try {
            System.out.println("Try agent register, " + name + ", " + type + ", " + dfd.getName().getName());
            DFService.register(agent, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public static AID getFromYellow(Agent myAgent, String type, String name) {
        //todo optimize
        AID[] resultAgents = new AID[1];
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            resultAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                resultAgents[i] = result[i].getName();
                System.out.println(resultAgents[i].getName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return resultAgents[0];
    }

    public static void deregister(Agent agent) {
        try {
            DFService.deregister(agent);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public static void stopAll() {
        for (int i = 0; i < agents.size(); i++) {
            agents.get(i).doDelete();
        }
    }
}
