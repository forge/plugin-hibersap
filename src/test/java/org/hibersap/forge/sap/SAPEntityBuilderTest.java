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

package org.hibersap.forge.sap;

import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Export;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.ParameterType;
import org.hibersap.annotations.Table;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.mapping.model.FieldMapping;
import org.hibersap.mapping.model.StructureMapping;
import org.hibersap.mapping.model.TableMapping;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * @author Max Schwaab
 */
public class SAPEntityBuilderTest {

    private static final String CLASS_NAME = "mySAPClass";
    private static final String JAVA_PACKAGE = "myPackage";

    private JavaClass javaClass;

    private BapiMapping createMapping() {
        final BapiMapping mapping = new BapiMapping( null, "BAPI_FLCONN_GETDETAIL", null );

        mapping.addImportParameter( new FieldMapping( String.class, "TRAVELAGENCYNUMBER", "_travelagencynumber", null ) );
        mapping.addImportParameter( new FieldMapping( String.class, "CONNECTIONNUMBER", "_connectionnumber", null ) );
        mapping.addImportParameter( new FieldMapping( String.class, "NO_AVAILIBILITY", "_noAvailibility", null ) );
        mapping.addImportParameter( new FieldMapping( Date.class, "FLIGHTDATE", "_flightdate", null ) );

//		final Set<FieldMapping> priceInfoParamters = new HashSet<FieldMapping>();
//		priceInfoParamters.add(new FieldMapping(BigDecimal.class, "PRICE_ECO2", "_priceEco2", null));
//		priceInfoParamters.add(new FieldMapping(String.class, "CURR", "_curr", null));
        mapping.addExportParameter( new StructureMapping( null, "PRICE_INFO", "_priceInfo", null ) );

//		final Set<FieldMapping> extOutParamters = new HashSet<FieldMapping>();
//		extOutParamters.add(new FieldMapping(String.class, "STRUCTURE", "_structure", null));
//		extOutParamters.add(new FieldMapping(String.class, "VALUEPART4", "_valuepart4", null));
        final StructureMapping structureMapping = new StructureMapping( null, "EXTENSION_OUT", "_extensionOut",
                                                                        null );
        mapping.addTableParameter( new TableMapping( List.class, null, "EXTENSION_OUT", "_extensionOut", structureMapping, null ) );

        return mapping;
    }

    @Before
    public void createSessionManager() {
        final BapiMapping mapping = createMapping();

        final SAPEntityBuilder builder = new SAPEntityBuilder();
        builder.createNew( SAPEntityBuilderTest.CLASS_NAME, SAPEntityBuilderTest.JAVA_PACKAGE, mapping );
        this.javaClass = builder.getSAPEntity().getBapiClass();
    }

    @Test
    public void createSAPEntityFromMappingWithoutImportParams() {
        final BapiMapping mapping = new BapiMapping( null, "BAPI_FLCONN_GETDETAIL", null );

//		final Set<FieldMapping> priceInfoParamters = new HashSet<FieldMapping>();
//		priceInfoParamters.add(new FieldMapping(BigDecimal.class, "PRICE_ECO2", "_priceEco2", null));
//		priceInfoParamters.add(new FieldMapping(String.class, "CURR", "_curr", null));
        mapping.addExportParameter( new StructureMapping( null, "PRICE_INFO", "_priceInfo", null ) );

//		final Set<FieldMapping> extOutParamters = new HashSet<FieldMapping>();
//		extOutParamters.add(new FieldMapping(String.class, "STRUCTURE", "_structure", null));
//		extOutParamters.add(new FieldMapping(String.class, "VALUEPART4", "_valuepart4", null));
        final StructureMapping structureMapping = new StructureMapping( null, "EXTENSION_OUT", "_extensionOut",
                                                                        null );
        mapping.addTableParameter( new TableMapping( List.class, null, "EXTENSION_OUT", "_extensionOut", structureMapping, null ) );

        final SAPEntityBuilder builder = new SAPEntityBuilder();

        builder.createNew( SAPEntityBuilderTest.CLASS_NAME, SAPEntityBuilderTest.JAVA_PACKAGE, mapping );

        Assert.assertThat( this.javaClass.getName(),
                           is( equalTo( SAPEntityBuilderTest.CLASS_NAME ) ) );
        Assert.assertThat( this.javaClass.getPackage(), equalTo( SAPEntityBuilderTest.JAVA_PACKAGE ) );
        Assert.assertThat( this.javaClass.getAnnotations().size(), is( 1 ) );
        Assert.assertThat( this.javaClass.getAnnotation( Bapi.class ), is( notNullValue() ) );
    }

    @Test
    public void createsBasicClassDeclarations() {
        Assert.assertThat( this.javaClass.getName(),
                           is( equalTo( SAPEntityBuilderTest.CLASS_NAME ) ) );
        Assert.assertThat( this.javaClass.getPackage(), equalTo( SAPEntityBuilderTest.JAVA_PACKAGE ) );
        Assert.assertThat( this.javaClass.getAnnotations().size(), is( 1 ) );
        Assert.assertThat( this.javaClass.getAnnotation( Bapi.class ), is( notNullValue() ) );
    }

    @Test
    public void createsAllFields() {
        final String[] allFieldNames = {"_travelagencynumber", "_noAvailibility", "_connectionnumber", "_flightdate",
                                        "_priceInfo", "_extensionOut"};

        Assert.assertThat( this.javaClass.getFields().size(), is( 6 ) );

        for ( final String fieldName : allFieldNames ) {
            Field<JavaClass> field = this.javaClass.getField( fieldName );
            Assert.assertThat( field, notNullValue() );
        }
    }

    @Test
    public void createsSimpleImportParameter() {
        final Field<JavaClass> field = this.javaClass.getField( "_flightdate" );

        Assert.assertThat( field.getType(), equalTo( "Date" ) );
        Assert.assertThat( field.getAnnotations().size(), is( 2 ) );
        Assert.assertThat( field.getAnnotation( Import.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ),
                           equalTo( "FLIGHTDATE" ) );
    }

    @Test
    public void createsComplexExportParameter() {
        final Field<JavaClass> field = this.javaClass.getField( "_priceInfo" );

        Assert.assertThat( field.getType(), equalTo( "PriceInfo" ) );
        Assert.assertThat( field.getAnnotations().size(), is( 2 ) );
        Assert.assertThat( field.getAnnotation( Export.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ),
                           equalTo( "PRICE_INFO" ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ).getEnumValue( ParameterType.class, "type" ),
                           equalTo( ParameterType.STRUCTURE ) );
    }

    @Test
    public void createsTableParamater() {
        final Field<JavaClass> field = this.javaClass.getField( "_extensionOut" );

        Assert.assertThat( field.getType(), equalTo( "List" ) );
        Assert.assertThat( field.getAnnotations().size(), is( 2 ) );
        Assert.assertThat( field.getAnnotation( Table.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ), is( notNullValue() ) );
        Assert.assertThat( field.getAnnotation( Parameter.class ).getStringValue( "value" ),
                           equalTo( "EXTENSION_OUT" ) );
    }
}
