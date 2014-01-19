/*
 * Copyright 2013 Nicolas Morel
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

package com.github.nmorel.gwtjackson.guava.client.deser;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.deser.map.key.KeyDeserializer;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.common.collect.ImmutableMultimap;

/**
 * Base {@link JsonDeserializer} implementation for {@link ImmutableMultimap}.
 *
 * @param <M> Type of the {@link ImmutableMultimap}
 * @param <K> Type of the keys inside the {@link ImmutableMultimap}
 * @param <V> Type of the values inside the {@link ImmutableMultimap}
 *
 * @author Nicolas Morel
 */
public abstract class BaseImmutableMultimapJsonDeserializer<M extends ImmutableMultimap<K, V>, K, V> extends JsonDeserializer<M> {

    /**
     * {@link KeyDeserializer} used to deserialize the keys.
     */
    protected final KeyDeserializer<K> keyDeserializer;

    /**
     * {@link JsonDeserializer} used to deserialize the values.
     */
    protected final JsonDeserializer<V> valueDeserializer;

    /**
     * @param keyDeserializer {@link KeyDeserializer} used to deserialize the keys.
     * @param valueDeserializer {@link JsonDeserializer} used to deserialize the values.
     */
    protected BaseImmutableMultimapJsonDeserializer( KeyDeserializer<K> keyDeserializer, JsonDeserializer<V> valueDeserializer ) {
        if ( null == keyDeserializer ) {
            throw new IllegalArgumentException( "keyDeserializer cannot be null" );
        }
        if ( null == valueDeserializer ) {
            throw new IllegalArgumentException( "valueDeserializer cannot be null" );
        }
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    /**
     * Build the {@link ImmutableMultimap} using the given builder.
     *
     * @param reader {@link JsonReader} used to read the JSON input
     * @param ctx Context for the full deserialization process
     * @param params Parameters for this deserialization
     * @param builder {@link ImmutableMultimap.Builder} used to collect the entries
     */
    protected void buildMultimap( JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params,
                                  ImmutableMultimap.Builder<K, V> builder ) {
        reader.beginObject();
        while ( JsonToken.END_OBJECT != reader.peek() ) {
            String name = reader.nextName();
            K key = keyDeserializer.deserialize( name, ctx );
            reader.beginArray();
            while ( JsonToken.END_ARRAY != reader.peek() ) {
                V value = valueDeserializer.deserialize( reader, ctx, params );
                builder.put( key, value );
            }
            reader.endArray();
        }
        reader.endObject();
    }

    @Override
    public void setBackReference( String referenceName, Object reference, M value, JsonDeserializationContext ctx ) {
        if ( null != value ) {
            for ( V val : value.values() ) {
                valueDeserializer.setBackReference( referenceName, reference, val, ctx );
            }
        }
    }
}
