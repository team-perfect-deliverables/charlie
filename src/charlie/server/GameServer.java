/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.server;

import charlie.actor.House;
import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.remote.ServerTopology;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roncoleman125
 */
public class GameServer {
    static {
        // For properties see http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html
        Properties _props = System.getProperties();
        _props.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        _props.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        _props.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
    }
    
    public static  Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final static String HOUSE_ACTOR = "HOUSE";
    private final static Random ran = new Random(0);
    private final static Integer TOPOLOGY_PORT = 1234;
    private final static Integer LOGIN_PORT = 9000;
    private final static String HOST = "127.0.0.1";
    private Properties props = new Properties();
    private final List<Ticket> logins = new ArrayList<>();

    public static void main(String[] args) {
        new GameServer().go();
    }
    
    public void go() {
        try {
            props.load(new FileInputStream("charlie.props"));
            
            Topology serverTopology = new ServerTopology(HOST, TOPOLOGY_PORT);
            LOG.info("server topology started...");

            House house = new House(this,props);

            Address houseAddr = serverTopology.spawnActor(HOUSE_ACTOR, house);

            ServerSocket serverSocket = new ServerSocket(LOGIN_PORT);
            
            LOG.info("game server started...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                LOG.info("processing login from "+clientSocket.getInetAddress());
                
                process(clientSocket, houseAddr);

            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void process(final Socket clientSocket, final Address house) {
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = null;
                    ObjectOutputStream oos = null;
                    try (InputStream is = clientSocket.getInputStream();
                            ObjectInputStream ois = new ObjectInputStream(is)) {
                        Login login = (Login) ois.readObject();
                        LOG.info("got login...");

                        Ticket ticket = validate(house, login);
                        
                        if (ticket != null) {  
                            LOG.info("validated login");
                            os = clientSocket.getOutputStream();
                            oos = new ObjectOutputStream(os);
                            oos.writeObject(ticket);
                            oos.flush();
                            LOG.info("sent ticket to client");
                            
                            logins.add(ticket);
                        }

                        ois.close();
                        is.close();

                    }
                    if (oos != null) {
                        oos.close();
                    }

                    if (os != null) {
                        os.close();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };

        new Thread(thread).start();
    }

    public List<Ticket> getLogins() {
        return logins;
    }
    private Ticket validate(Address house, Login login) {
        if (login.getLogname() != null && login.getPassword() != null) {
            return new Ticket(house,ran.nextLong(),500.0);
        }

        return null;
    }
}
