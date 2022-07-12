package config;

import config.models.ConfigYaml;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.yaml.snakeyaml.constructor.Constructor;
public class ConfigHandler {

    public static final Path configPath = Paths.get("./config.yml");

    private static ConfigHandler configHandler;

    ConfigYaml config;

    /**
     * Get instance of ConfigHandler
     * @return
     * @throws FileNotFoundException
     */
    public static ConfigHandler getInstance() throws FileNotFoundException {
        return getInstance(configPath);
    }

    /**
     * Get instance of ConfigHandler
     * @param configPath
     * @return
     * @throws FileNotFoundException
     */
    public static ConfigHandler getInstance(Path configPath) throws FileNotFoundException {
        if(configHandler == null) {
            configHandler = new ConfigHandler(configPath);
        }
        return configHandler;
    }

    /**
     * Constructor
     * @param configPath
     * @throws FileNotFoundException
     */
    private ConfigHandler(Path configPath) throws FileNotFoundException {
        this.config = loadConfig(configPath);
    }

    /**
     * Load config.yml
     * @param configPath
     * @throws FileNotFoundException
     */
    public ConfigYaml loadConfig(Path configPath) throws FileNotFoundException {
        Constructor constructor = new Constructor(ConfigYaml.class);
        Yaml yaml = new Yaml(constructor);
        return yaml.load(new FileInputStream(configPath.toFile()));
    }

//    /**
//     * Dump config to config.yml
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     * @throws IOException
//     */
//    public void dumpConfig() throws IllegalArgumentException, IllegalAccessException, IOException {
//        dumpConfig(this.config, configPath);
//    }

//    /**
//     * Dump config to config.yml
//     * @param configPath
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     * @throws IOException
//     */
//    public void dumpConfig(Config config, Path configPath) throws IllegalArgumentException, IllegalAccessException, IOException {
//        DumperOptions options = new DumperOptions();
//        options.setDefaultFlowStyle(FlowStyle.BLOCK);
//        options.setPrettyFlow(true);
//        Yaml yml = new Yaml(options);
//        yml.dump(config, new FileWriter(configPath.toFile()));
//    }

    /**
     * Get config object
     * @return
     */
    public ConfigYaml getConfig() {
        return this.config;
    }

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException, NoSuchFieldException, SecurityException {
        ConfigHandler handler = ConfigHandler.getInstance();
        ConfigYaml config = handler.getConfig();
        System.out.println("TELEGRAM: "+config.getTelegramToken());
        System.out.println("USER: "+config.getUserId());
        System.out.println("IMMOBILIARE: "+ config.getWebsites().get("immobiliare").getUrl());
//        handler.dumpConfig();

    }
}