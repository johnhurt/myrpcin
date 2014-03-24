/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Json Serializable config file for first java rpc app
 *
 * @author kguthrie
 */
public final class Config {

    private String centerpointLocator;
    private String endpointLocator;

    transient private File file;

    public Config() {
    }

    /**
     * Open the config in the given file. If no file is found, then the default
     * config file will be written there
     *
     * @param file
     * @throws java.io.IOException
     */
    public Config(File file) throws IOException {
        this.file = file;
        ObjectMapper mapper = new ObjectMapper();

        // If the given file does not exist, then generate one with a centerpont
        // provided by the user
        if (!file.exists()) {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.println(
                    "Enter the locator for the centerpoint you "
                    + "want to connect with.");
            System.out.println(
                    "You can find this by signing into http://myrpc.in");
            centerpointLocator = br.readLine();

            mapper.writeValue(file, this);
            return;
        }

        Config other = mapper.readValue(file, Config.class);
        this.centerpointLocator = other.centerpointLocator;
        this.endpointLocator = other.endpointLocator;
    }

    /**
     * @return the centerpointLocator
     */
    public String getCenterpointLocator() {
        return centerpointLocator;
    }

    /**
     * @param centerpointLocator the centerpointLocator to set
     */
    public void setCenterpointLocator(String centerpointLocator) {
        this.centerpointLocator = centerpointLocator;
    }

    /**
     * @return the endpointLocator
     */
    public String getEndpointLocator() {
        return endpointLocator;
    }

    /**
     * @param endpointLocator the endpointLocator to set
     */
    public void setEndpointLocator(String endpointLocator) {
        this.endpointLocator = endpointLocator;
    }

    /**
     * Save this config to the file with which it was opened
     * @throws java.io.IOException
     */
    public void save() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, this);
    }
}
