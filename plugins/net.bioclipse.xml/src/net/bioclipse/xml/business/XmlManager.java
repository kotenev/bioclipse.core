/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.xml.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlManager implements IBioclipseManager {

    private static final Logger logger = Logger.getLogger(XmlManager.class);

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "xml";
    }

    public boolean isWellFormed(IFile file, IProgressMonitor monitor)
        throws BioclipseException, CoreException {
        logger.debug("Checking for well-formedness");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new DummyErrorHandler());

            Builder builder = new Builder(reader);
            builder.build(file.getContents());
        } catch (ValidityException exception) {
            logger.debug(exception.getMessage());
            return false;
        } catch (ParsingException exception) {
            logger.debug(exception.getMessage());
            return false;
        } catch (SAXException exception) {
            logger.debug(exception.getMessage());
            return false;
        } catch (IOException exception) {
            throw new BioclipseException(
                "Error while opening file",
                exception
            );
        } catch (CoreException exception) {
            throw new BioclipseException(
                "Error while opening file",
                exception
            );
        } catch (ParserConfigurationException exception) {
            throw new BioclipseException(
                "Error while setting up validatin engine",
                exception
            );
        }
        return true;
    }

    public boolean isValid(IFile file, IProgressMonitor monitor)
    throws BioclipseException, CoreException {
        logger.debug("Checking for validate");
        try {
            XMLReader xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser"); 
            xerces.setFeature("http://apache.org/xml/features/validation/schema", true);                         

            Builder parser = new Builder(xerces, true);
            parser.build(file.getContents());
        } catch (ValidityException exception) {
            return false;
        } catch (ParsingException exception) {
            return false;
        } catch (IOException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (CoreException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (SAXException exception) {
            throw new BioclipseException(
                "Error creating Xerces parser",
                exception
            );
        }
        return true;
    }

    public List<XMLError> validate(IFile file, IProgressMonitor monitor)
    throws BioclipseException, CoreException {
        List<XMLError> errors = new ArrayList<XMLError>();
        logger.debug("Checking for validness");
        try {
            XMLReader xerces = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser"
            ); 
            xerces.setFeature(
                "http://apache.org/xml/features/validation/schema",
                true
            );                         

            Builder parser = new Builder(xerces, true);
            parser.build(file.getContents());
        } catch (ValidityException exception) {
            int errorCount = exception.getErrorCount();
            for (int i=0; i<errorCount; i++) {
                errors.add(new XMLError(exception.getValidityError(i)));
            }
        } catch (ParsingException exception) {
            errors.add(new XMLError(exception.getMessage()));
        } catch (IOException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (CoreException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (SAXException exception) {
            throw new BioclipseException(
                "Error creating Xerces parser",
                exception
            );
        }
        return errors;
    }

    public List<XMLError> validateAgainstXMLSchema(
        IFile file, IFile schema, IProgressMonitor monitor)
    throws BioclipseException, CoreException {
        List<XMLError> errors = new ArrayList<XMLError>();
        logger.debug("Checking for validness");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SchemaFactory schemaFactory = 
                SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            factory.setSchema(schemaFactory.newSchema(
                new Source[] {new StreamSource(schema.getContents())})
            );

            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            SimpleErrorHandler errorHandler = new SimpleErrorHandler();
            reader.setErrorHandler(errorHandler);
            
            Builder builder = new Builder(reader);
            builder.build(file.getContents());
            errors.addAll(errorHandler.getErrors());
        } catch (ValidityException exception) {
            int errorCount = exception.getErrorCount();
            for (int i=0; i<errorCount; i++) {
                errors.add(new XMLError(exception.getValidityError(i)));
            }
        } catch (ParsingException exception) {
            errors.add(new XMLError(exception.getMessage()));
        } catch (IOException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (CoreException exception) {
            throw new BioclipseException(
                    "Error while opening file",
                    exception
            );
        } catch (SAXException exception) {
            throw new BioclipseException(
                "Error creating Xerces parser",
                exception
            );
        } catch (ParserConfigurationException exception) {
            throw new BioclipseException(
                "Error while setting up validatin engine",
                exception
            );
        }
        return errors;
    }

}
