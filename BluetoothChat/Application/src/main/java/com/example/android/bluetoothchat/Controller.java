package com.example.android.bluetoothchat;

import android.content.Context;

import com.example.android.common.logger.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.IConnectionListener;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.network.ConnectionImpl;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class Controller {

    private static final Controller instance = new Controller();

    private Publisher publisher;

    public Controller (){
    }

    public static Controller getInstance() {
        return instance;
    }

    //Variaveis para uso do CDDL
    protected ConnectionImpl con;
    protected String host;
    private CDDL cddl;
    private Subscriber subscriber;
    //private EventBus eventBus;
    BluetoothChatService btChat;

    public void initCDDL(Context context) {

        host = "broker.hivemq.com";
        con = ConnectionFactory.createConnection();
        con.setClientId("lucasalves");
        con.setHost(host);
        con.addConnectionListener(connectionListener);
        con.connect();

        cddl = CDDL.getInstance();
        cddl.setContext(context);
        cddl.setConnection(con);
        cddl.startService();

        createPublish();
    }


    private final IConnectionListener connectionListener = new IConnectionListener() {

        @Override
        public void onConnectionEstablished() {
            addLogOnView("Conexão estabelecida.");
        }

        @Override
        public void onConnectionEstablishmentFailed() {
            addLogOnView("Falha na conexão.");
        }

        @Override
        public void onConnectionLost() {
            addLogOnView("Conexão perdida.");
        }

        @Override
        public void onDisconnectedNormally() {
            addLogOnView("Uma desconexão normal ocorreu.");
        }
    };


    public void addLogOnView(String text) {
        Log.d("resposta: ", text);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(String message){
        Log.i("Chegou Mensagem", "resposta-controle");
        addLogOnView(message);
    }

    protected void publishMessage(String uuid, String serviceName) {
        Message message = new Message();
        message.setServiceName(serviceName);
        message.setServiceValue(uuid);
        publisher.publish(message);
    }

    private void createPublish(){
        publisher = PublisherFactory.createPublisher();
        publisher.addConnection(cddl.getConnection());
    }

    public void listenerMessage() {
        subscriber.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {
                //eventBus.post(message);
                Log.d("resposta " , message.getAvailableAttributesList()[0]);
                btChat = BluetoothChatService.getInstance();
                String resposta = message.getAvailableAttributesList()[0];
                btChat.write(resposta.getBytes());
            }
        });
    }



    public void subscribeService(String serviceName){
        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());
        subscriber.subscribeServiceByName(serviceName);
    }
}