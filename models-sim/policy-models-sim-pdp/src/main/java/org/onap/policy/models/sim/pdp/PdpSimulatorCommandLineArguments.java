/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.sim.pdp;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.chainsaw.Main;

import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorRunTimeException;

/**
 * This class reads and handles command line parameters for the pdp simulator.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpSimulatorCommandLineArguments {

    private static final String FILE_MESSAGE_PREAMBLE = " file \"";
    private static final int HELP_LINE_LENGTH = 120;

    private final Options options;
    private String configurationFilePath = null;
    private String propertyFilePath = null;

    /**
     * Construct the options for the CLI editor.
     */
    public PdpSimulatorCommandLineArguments() {
        //@formatter:off
        options = new Options();
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("outputs the usage of this command")
                .required(false)
                .type(Boolean.class)
                .build());
        options.addOption(Option.builder("v")
                .longOpt("version")
                .desc("outputs the version of pdp simulator")
                .required(false)
                .type(Boolean.class)
                .build());
        options.addOption(Option.builder("c")
                .longOpt("config-file")
                .desc("the full path to the configuration file to use, "
                        + "the configuration file must be a Json file containing the pdp simulator parameters")
                .hasArg()
                .argName("CONFIG_FILE")
                .required(false)
                .type(String.class)
                .build());
        options.addOption(Option.builder("p")
                .longOpt("property-file")
                .desc("the full path to the topic property file to use, "
                        + "the property file contains the pdp simulator topic properties")
                .hasArg()
                .argName("PROP_FILE")
                .required(false)
                .type(String.class)
                .build());
        //@formatter:on
    }

    /**
     * Construct the options for the CLI editor and parse in the given arguments.
     *
     * @param args The command line arguments
     */
    public PdpSimulatorCommandLineArguments(final String[] args) {
        // Set up the options with the default constructor
        this();

        // Parse the arguments
        try {
            parse(args);
        } catch (final PdpSimulatorException e) {
            throw new PdpSimulatorRunTimeException("parse error on pdp simulator parameters", e);
        }
    }

    /**
     * Parse the command line options.
     *
     * @param args The command line arguments
     * @return a string with a message for help and version, or null if there is no message
     * @throws PdpSimulatorException on command argument errors
     */
    public String parse(final String[] args) throws PdpSimulatorException {
        // Clear all our arguments
        setConfigurationFilePath(null);
        setPropertyFilePath(null);

        CommandLine commandLine = null;
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (final ParseException e) {
            throw new PdpSimulatorException("invalid command line arguments specified : " + e.getMessage());
        }

        // Arguments left over after Commons CLI does its stuff
        final String[] remainingArgs = commandLine.getArgs();

        if (remainingArgs.length > 0 && commandLine.hasOption('c') || remainingArgs.length > 0) {
            throw new PdpSimulatorException("too many command line arguments specified : " + Arrays.toString(args));
        }

        if (remainingArgs.length == 1) {
            configurationFilePath = remainingArgs[0];
        }

        if (commandLine.hasOption('h')) {
            return help(Main.class.getName());
        }

        if (commandLine.hasOption('v')) {
            return version();
        }

        if (commandLine.hasOption('c')) {
            setConfigurationFilePath(commandLine.getOptionValue('c'));
        }

        if (commandLine.hasOption('p')) {
            setPropertyFilePath(commandLine.getOptionValue('p'));
        }

        return null;
    }

    /**
     * Validate the command line options.
     *
     * @throws PdpSimulatorException on command argument validation errors
     */
    public void validate() throws PdpSimulatorException {
        validateReadableFile("pdp simulator configuration", configurationFilePath);
    }

    /**
     * Print version information for pdp simulator.
     *
     * @return the version string
     */
    public String version() {
        return ResourceUtils.getResourceAsString("src/main/resources/version.txt");
    }

    /**
     * Print help information for pdp simulator.
     *
     * @param mainClassName the main class name
     * @return the help string
     */
    public String help(final String mainClassName) {
        final HelpFormatter helpFormatter = new HelpFormatter();
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        helpFormatter.printHelp(printWriter, HELP_LINE_LENGTH, mainClassName + " [options...]", "options", options, 0,
                0, "");

        return stringWriter.toString();
    }

    /**
     * Gets the configuration file path.
     *
     * @return the configuration file path
     */
    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    /**
     * Gets the full expanded configuration file path.
     *
     * @return the configuration file path
     */
    public String getFullConfigurationFilePath() {
        return ResourceUtils.getFilePath4Resource(getConfigurationFilePath());
    }

    /**
     * Sets the configuration file path.
     *
     * @param configurationFilePath the configuration file path
     */
    public void setConfigurationFilePath(final String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;

    }

    /**
     * Check set configuration file path.
     *
     * @return true, if check set configuration file path
     */
    public boolean checkSetConfigurationFilePath() {
        return configurationFilePath != null && configurationFilePath.length() > 0;
    }

    /**
     * Gets the property file path.
     *
     * @return the property file path
     */
    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    /**
     * Gets the full expanded property file path.
     *
     * @return the property file path
     */
    public String getFullPropertyFilePath() {
        return ResourceUtils.getFilePath4Resource(getPropertyFilePath());
    }

    /**
     * Sets the property file path.
     *
     * @param propertyFilePath the property file path
     */
    public void setPropertyFilePath(final String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;

    }

    /**
     * Check set property file path.
     *
     * @return true, if check set property file path
     */
    public boolean checkSetPropertyFilePath() {
        return propertyFilePath != null && propertyFilePath.length() > 0;
    }

    /**
     * Validate readable file.
     *
     * @param fileTag the file tag
     * @param fileName the file name
     * @throws PdpSimulatorException on the file name passed as a parameter
     */
    private void validateReadableFile(final String fileTag, final String fileName) throws PdpSimulatorException {
        if (fileName == null || fileName.length() == 0) {
            throw new PdpSimulatorException(fileTag + " file was not specified as an argument");
        }

        // The file name refers to a resource on the local file system
        final URL fileUrl = ResourceUtils.getUrl4Resource(fileName);
        if (fileUrl == null) {
            throw new PdpSimulatorException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" does not exist");
        }

        final File theFile = new File(fileUrl.getPath());
        if (!theFile.exists()) {
            throw new PdpSimulatorException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" does not exist");
        }
        if (!theFile.isFile()) {
            throw new PdpSimulatorException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" is not a normal file");
        }
        if (!theFile.canRead()) {
            throw new PdpSimulatorException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" is ureadable");
        }
    }

}
