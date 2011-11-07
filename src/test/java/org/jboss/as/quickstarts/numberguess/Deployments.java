package org.jboss.as.quickstarts.numberguess;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments {
    private static final String ARCHIVE_NAME = "jboss-as-numberguess-nocdi.war";
    private static final String BUILD_DIRECTORY = "target";

    public static WebArchive createDeployment() {
        return ShrinkWrap.create(ZipImporter.class, ARCHIVE_NAME).importFrom(new File(BUILD_DIRECTORY + '/' + ARCHIVE_NAME))
        .as(WebArchive.class);
    }
}
