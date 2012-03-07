/*
 * Copyright (C) 2012 akquinet AG
 *
 * This file is part of the Forge Hibersap Plugin.
 *
 * The Forge Hibersap Plugin is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Forge Hibersap Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the Forge Hibersap Plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibersap.forge.plugin.sap;

import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Export;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.ParameterType;
import org.hibersap.annotations.Table;
import org.hibersap.forge.plugin.sap.SAPEntityBuilder;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.mapping.model.FieldMapping;
import org.hibersap.mapping.model.StructureMapping;
import org.hibersap.mapping.model.TableMapping;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * 
 * @author Max Schwaab
 *
 */
public class SAPEntityBuilderTest
{


    private static final String CLASS_NAME = "mySAPClass";
    private static final String JAVA_PACKAGE = "myPackage";


    private JavaClass javaClass;

    private BapiMapping createMapping()
    {
        final BapiMapping mapping = new BapiMapping( null, "BAPI_FLCONN_GETDETAIL", null );

        mapping.addImportParameter(
                new FieldMapping( String.class, "TRAVELAGENCYNUMBER", "_travelagencynumber", null ) );
        mapping.addImportParameter( new FieldMapping( String.class, "CONNECTIONNUMBER", "_connectionnumber", null ) );
        mapping.addImportParameter( new FieldMapping( String.class, "NO_AVAILIBILITY", "_noAvailibility", null ) );
        mapping.addImportParameter( new FieldMapping( Date.class, "FLIGHTDATE", "_flightdate", null ) );

        final Set<FieldMapping> priceInfoParamters = new HashSet<FieldMapping>();
        priceInfoParamters.add( new FieldMapping( BigDecimal.class, "PRICE_ECO2", "_priceEco2", null ) );
        priceInfoParamters.add( new FieldMapping( String.class, "CURR", "_curr", null ) );
        mapping.addExportParameter( new StructureMapping( null, "PRICE_INFO", "_priceInfo", priceInfoParamters ) );

        final Set<FieldMapping> extOutParamters = new HashSet<FieldMapping>();
        extOutParamters.add( new FieldMapping( String.class, "STRUCTURE", "_structure", null ) );
        extOutParamters.add( new FieldMapping( String.class, "VALUEPART4", "_valuepart4", null ) );
        final StructureMapping structureMapping = new StructureMapping( null, "EXTENSION_OUT", "_extensionOut",
                extOutParamters );
        mapping.addTableParameter(
                new TableMapping( List.class, null, "EXTENSION_OUT", "_extensionOut", structureMapping ) );


        return mapping;
    }

    @Before
    public void createSessionManager()
    {
        final BapiMapping mapping = createMapping();

        final SAPEntityBuilder builder = new SAPEntityBuilder();
        builder.createNew( CLASS_NAME, JAVA_PACKAGE, mapping );
        javaClass = builder.getSAPEntity().getBapiClass();
    }

    @Test
    public void createsBasicClassDeclarations()
    {
        assertThat( javaClass.getName(), is( equalTo( CLASS_NAME ) ) );
        assertThat( javaClass.getPackage(), equalTo( JAVA_PACKAGE ) );
        assertThat( javaClass.getAnnotations().size(), is( 1 ) );
        assertThat( javaClass.getAnnotation( Bapi.class ), is( notNullValue() ) );
    }

    @Test
    public void createsAllFields()
    {
        String[] allFieldNames = {
                "_travelagencynumber",
                "_noAvailibility",
                "_connectionnumber",
                "_flightdate",
                "_priceInfo",
                "_extensionOut",
        };

        assertThat( javaClass.getFields().size(), is( 6 ) );

        for ( final String fieldName : allFieldNames )
        {
            assertThat( javaClass.getField( fieldName ), notNullValue() );
        }
    }

    @Test
    public void createsSimpleImportParameter()
    {
        final Field<JavaClass> field = javaClass.getField( "_flightdate" );

        assertThat( field.getType(), equalTo( "Date" ) );
        assertThat( field.getAnnotations().size(), is( 2 ) );
        assertThat( field.getAnnotation( Import.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ), equalTo( "FLIGHTDATE" ) );
    }

    @Test
    public void createsComplexExportParameter()
    {
        final Field<JavaClass> field = javaClass.getField( "_priceInfo" );

        assertThat( field.getType(), equalTo( "PriceInfo" ) );
        assertThat( field.getAnnotations().size(), is( 2 ) );
        assertThat( field.getAnnotation( Export.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ), equalTo( "PRICE_INFO" ) );
        assertThat( field.getAnnotation( Parameter.class ).getEnumValue( ParameterType.class, "type" ), equalTo( ParameterType.STRUCTURE ) );
    }

    @Test
    public void createsTableParamater()
    {
        final Field<JavaClass> field = javaClass.getField( "_extensionOut" );

        assertThat( field.getType(), equalTo( "List" ) );
        assertThat( field.getAnnotations().size(), is( 2 ) );
        assertThat( field.getAnnotation( Table.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ), equalTo( "EXTENSION_OUT" ) );
    }
}
