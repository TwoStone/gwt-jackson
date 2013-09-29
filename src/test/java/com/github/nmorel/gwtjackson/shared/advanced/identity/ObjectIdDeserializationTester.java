package com.github.nmorel.gwtjackson.shared.advanced.identity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.github.nmorel.gwtjackson.shared.AbstractTester;
import com.github.nmorel.gwtjackson.shared.JsonDecoderTester;
import com.github.nmorel.gwtjackson.shared.JsonMapperTester;

/**
 * Test from jackson-databind and adapted for the project
 */
public final class ObjectIdDeserializationTester extends AbstractTester {
    // // Classes for external id use

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    public static class Identifiable {

        public int value;

        public Identifiable next;

        public Identifiable() {
            this( 0 );
        }

        public Identifiable( int v ) {
            value = v;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "#")
    public static class UUIDNode {

        public int value;

        public UUIDNode parent;

        public UUIDNode first;

        public UUIDNode second;

        public UUIDNode() {
            this( 0 );
        }

        public UUIDNode( int v ) {
            value = v;
        }
    }

    // // Classes for external id from property annotations:

    public static class IdWrapper {

        @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
        public ValueNode node;

        public IdWrapper() {
        }

        public IdWrapper( int v ) {
            node = new ValueNode( v );
        }
    }

    public static class ValueNode {

        public int value;

        public IdWrapper next;

        public ValueNode() {
            this( 0 );
        }

        public ValueNode( int v ) {
            value = v;
        }
    }

    // // Classes for external id use

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "customId")
    public static class IdentifiableCustom {

        public int value;

        public int customId;

        public IdentifiableCustom next;

        public IdentifiableCustom() {
            this( -1, 0 );
        }

        public IdentifiableCustom( int i, int v ) {
            customId = i;
            value = v;
        }
    }

    public static class IdWrapperExt {

        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "customId")
        public ValueNodeExt node;

        public IdWrapperExt() {
        }

        public IdWrapperExt( int v ) {
            node = new ValueNodeExt( v );
        }
    }

    public static class ValueNodeExt {

        public int value;

        public IdWrapperExt next;

        private int customId;

        public ValueNodeExt() {
            this( 0 );
        }

        public ValueNodeExt( int v ) {
            value = v;
        }

        public void setCustomId( int i ) {
            customId = i;
        }
    }

    /*
    /**********************************************************
    /* Constantes
    /**********************************************************
     */
    public static final ObjectIdDeserializationTester INSTANCE = new ObjectIdDeserializationTester();

    private final static String EXP_SIMPLE_INT_CLASS = "{\"id\":1,\"value\":13,\"next\":1}";

    // Bit more complex, due to extra wrapping etc:
    private final static String EXP_SIMPLE_INT_PROP = "{\"node\":{\"@id\":1,\"value\":7,\"next\":{\"node\":1}}}";

    private final static String EXP_CUSTOM_VIA_CLASS = "{\"customId\":123,\"value\":-900,\"next\":123}";

    private final static String EXP_CUSTOM_VIA_PROP = "{\"node\":{\"customId\":3,\"value\":99,\"next\":{\"node\":3}}}";

    private ObjectIdDeserializationTester() {
    }

    /*
    /*****************************************************
    /* Unit tests, external id deserialization
    /*****************************************************
     */

    public void testSimpleDeserializationClass( JsonDecoderTester<Identifiable> mapper ) {
        // then bring back...
        Identifiable result = mapper.decode( EXP_SIMPLE_INT_CLASS );
        assertEquals( 13, result.value );
        assertSame( result, result.next );
    }

    public void testSimpleUUIDForClassRoundTrip( JsonMapperTester<UUIDNode> mapper ) {
        UUIDNode root = new UUIDNode( 1 );
        UUIDNode child1 = new UUIDNode( 2 );
        UUIDNode child2 = new UUIDNode( 3 );
        root.first = child1;
        root.second = child2;
        child1.parent = root;
        child2.parent = root;
        child1.first = child2;

        String json = mapper.encode( root );

        // and should come back the same too...
        UUIDNode result = mapper.decode( json );
        assertEquals( 1, result.value );
        UUIDNode result2 = result.first;
        UUIDNode result3 = result.second;
        assertNotNull( result2 );
        assertNotNull( result3 );
        assertEquals( 2, result2.value );
        assertEquals( 3, result3.value );

        assertSame( result, result2.parent );
        assertSame( result, result3.parent );
        assertSame( result3, result2.first );
    }

    /*
    /*****************************************************
    /* Unit tests, custom (property-based) id deserialization
    /*****************************************************
     */

    public void testSimpleDeserializationProperty( JsonDecoderTester<IdWrapper> mapper ) {
        IdWrapper result = mapper.decode( EXP_SIMPLE_INT_PROP );
        assertEquals( 7, result.node.value );
        assertSame( result.node, result.node.next.node );
    }

    // Another test to ensure ordering is not required (i.e. can do front references)
    public void testSimpleDeserWithForwardRefs( JsonDecoderTester<IdWrapper> mapper ) {
        IdWrapper result = mapper.decode( "{\"node\":{\"value\":7,\"next\":{\"node\":1}, \"@id\":1}}" );
        assertEquals( 7, result.node.value );
        assertSame( result.node, result.node.next.node );
    }

    public void testCustomDeserializationClass( JsonDecoderTester<IdentifiableCustom> mapper ) {
        // then bring back...
        IdentifiableCustom result = mapper.decode( EXP_CUSTOM_VIA_CLASS );
        assertEquals( -900, result.value );
        assertSame( result, result.next );
    }

    public void testCustomDeserializationProperty( JsonDecoderTester<IdWrapperExt> mapper ) {
        // then bring back...
        IdWrapperExt result = mapper.decode( EXP_CUSTOM_VIA_PROP );
        assertEquals( 99, result.node.value );
        assertSame( result.node, result.node.next.node );
        assertEquals( 3, result.node.customId );
    }
}