/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.state.internals;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.TimestampedKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class ReadOnlyKeyValueStoreFacadeTest {
    @Mock
    private TimestampedKeyValueStore<String, String> mockedKeyValueTimestampStore;
    @Mock
    private KeyValueIterator<String, ValueAndTimestamp<String>> mockedKeyValueTimestampIterator;

    private ReadOnlyKeyValueStoreFacade<String, String> readOnlyKeyValueStoreFacade;

    @BeforeEach
    public void setup() {
        readOnlyKeyValueStoreFacade = new ReadOnlyKeyValueStoreFacade<>(mockedKeyValueTimestampStore);
    }

    @Test
    public void shouldReturnPlainValueOnGet() {
        when(mockedKeyValueTimestampStore.get("key"))
            .thenReturn(ValueAndTimestamp.make("value", 42L));
        when(mockedKeyValueTimestampStore.get("unknownKey"))
            .thenReturn(null);

        assertThat(readOnlyKeyValueStoreFacade.get("key"), is("value"));
        assertNull(readOnlyKeyValueStoreFacade.get("unknownKey"));
    }

    @Test
    public void shouldReturnPlainKeyValuePairsForRangeIterator() {
        when(mockedKeyValueTimestampIterator.next())
            .thenReturn(KeyValue.pair("key1", ValueAndTimestamp.make("value1", 21L)))
            .thenReturn(KeyValue.pair("key2", ValueAndTimestamp.make("value2", 42L)));
        when(mockedKeyValueTimestampStore.range("key1", "key2")).thenReturn(mockedKeyValueTimestampIterator);

        final KeyValueIterator<String, String> iterator = readOnlyKeyValueStoreFacade.range("key1", "key2");
        assertThat(iterator.next(), is(KeyValue.pair("key1", "value1")));
        assertThat(iterator.next(), is(KeyValue.pair("key2", "value2")));
    }

    @Test
    public void shouldReturnPlainKeyValuePairsForPrefixScan() {
        final StringSerializer stringSerializer = new StringSerializer();
        when(mockedKeyValueTimestampIterator.next())
            .thenReturn(KeyValue.pair("key1", ValueAndTimestamp.make("value1", 21L)))
            .thenReturn(KeyValue.pair("key2", ValueAndTimestamp.make("value2", 42L)));
        when(mockedKeyValueTimestampStore.prefixScan("key", stringSerializer)).thenReturn(mockedKeyValueTimestampIterator);

        final KeyValueIterator<String, String> iterator = readOnlyKeyValueStoreFacade.prefixScan("key", stringSerializer);
        assertThat(iterator.next(), is(KeyValue.pair("key1", "value1")));
        assertThat(iterator.next(), is(KeyValue.pair("key2", "value2")));
    }

    @Test
    public void shouldReturnPlainKeyValuePairsForAllIterator() {
        when(mockedKeyValueTimestampIterator.next())
            .thenReturn(KeyValue.pair("key1", ValueAndTimestamp.make("value1", 21L)))
            .thenReturn(KeyValue.pair("key2", ValueAndTimestamp.make("value2", 42L)));
        when(mockedKeyValueTimestampStore.all()).thenReturn(mockedKeyValueTimestampIterator);

        final KeyValueIterator<String, String> iterator = readOnlyKeyValueStoreFacade.all();
        assertThat(iterator.next(), is(KeyValue.pair("key1", "value1")));
        assertThat(iterator.next(), is(KeyValue.pair("key2", "value2")));
    }

    @Test
    public void shouldForwardApproximateNumEntries() {
        when(mockedKeyValueTimestampStore.approximateNumEntries()).thenReturn(42L);

        assertThat(readOnlyKeyValueStoreFacade.approximateNumEntries(), is(42L));
    }
}
