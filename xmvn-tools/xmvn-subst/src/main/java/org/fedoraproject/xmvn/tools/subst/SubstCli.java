/*-
 * Copyright (c) 2013-2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.xmvn.tools.subst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mikolaj Izdebski
 */
@Named
@Singleton
public class SubstCli
{
    private final Logger logger = LoggerFactory.getLogger( SubstCli.class );

    private final ArtifactVisitor visitor;

    @Inject
    public SubstCli( ArtifactVisitor visitor )
    {
        this.visitor = visitor;
    }

    private void run( SubstCliRequest cliRequest )
    {
        visitor.setTypes( cliRequest.getTypes() );
        visitor.setFollowSymlinks( cliRequest.isFollowSymlinks() );
        visitor.setDryRun( cliRequest.isDryRun() );

        try
        {
            for ( String path : cliRequest.getParameters() )
            {
                Files.walkFileTree( Paths.get( path ), visitor );
            }
        }
        catch ( IOException e )
        {
            logger.error( "I/O error occured", e );
        }

        if ( cliRequest.isStrict() && visitor.getFailureCount() > 0 )
            System.exit( 1 );
    }

    public static void main( String[] args )
    {
        try
        {
            SubstCliRequest cliRequest = new SubstCliRequest( args );

            Module module = new WireModule( new SpaceModule( new URLClassSpace( SubstCli.class.getClassLoader() ) ) );
            Injector injector = Guice.createInjector( module );
            SubstCli cli = injector.getInstance( SubstCli.class );

            cli.run( cliRequest );
        }
        catch ( Throwable e )
        {
            System.err.println( "Unhandled exception" );
            e.printStackTrace();
            System.exit( 2 );
        }
    }
}
