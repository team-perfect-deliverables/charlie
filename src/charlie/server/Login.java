/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charlie.server;

import java.io.Serializable;

/**
 *
 * @author roncoleman125
 */
public class Login implements Serializable {
    private String logname;
    private String password;
    
    public Login() {
    }
    
    public Login(String logname,String password) {
        this.logname = logname;
        this.password = password;
    }

    public String getLogname() {
        return logname;
    }

    public String getPassword() {
        return password;
    }
}
