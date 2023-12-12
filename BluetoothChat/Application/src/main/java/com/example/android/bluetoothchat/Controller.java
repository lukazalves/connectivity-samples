package com.example.android.bluetoothchat;

import android.content.Context;
import android.os.Handler;

import com.example.android.common.logger.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
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
    private Message message;

    public Controller() {
        eventBus = EventBus.builder().build();
        eventBus.register(this);
    }

    public static Controller getInstance() {
        return instance;
    }

    //Variaveis para uso do CDDL
    private ConnectionImpl con;
    private String host;
    private CDDL cddl;
    private Subscriber subscriber;
    private EventBus eventBus;
    BluetoothChatService btChat;

    public void initCDDL(Context context) {

        //host = CDDL.startMicroBroker();
        //host = "broker.mqttdashboard.com";
        //host = "test.mosquitto.org";
        con = ConnectionFactory.createConnection();
        con.setClientId("lucas.silva");
        //con.setHost(host);
        con.setHost(Connection.DEFAULT_HOST);
        con.addConnectionListener(connectionListener);
        con.connect();

        cddl = CDDL.getInstance();
        cddl.setConnection(con);
        cddl.setContext(context);
        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);

        createPublish();
    }


    private IConnectionListener connectionListener = new IConnectionListener() {

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
        Log.d("Chegou Mensagem", "resposta-controle");
        addLogOnView(message);
    }

    public void publishMessage(String uuid, String serviceName) {
        message = new Message();
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
                //String serviceName = message.getServiceName();
                //String atributo = message.getAvailableAttributesList()[0];
                //Double serviceValue = (Double) message.getServiceValue();
                //addLogOnView();
                //Log.d("Resposta: ServiceName: ",serviceName );
                //Log.d("Resposta: atributo:  ", atributo );
                //Log.d("Resposta: serviceValue: ", serviceValue.toString() );
                eventBus.post(message);
                Log.d("resposta " , message.getServiceValue()[0].toString());
                BluetoothChatService btChat = BluetoothChatService.getInstance();
                String resposta = message.getServiceValue()[0].toString();
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