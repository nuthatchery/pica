package org.magnolialang.memo.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.magnolialang.memo.SoftHashTable;
import org.magnolialang.testutil.generators.BasicGenerator;

public class SoftHashTableTest {
	private static int N = 1000;
	private static int M = 1000;
	private final List<DataEntry> data = new ArrayList<DataEntry>();


	@Before
	public void setup() {
		for(int i = 0; i < N; i++) {
			SoftHashTable<Integer, Integer> table = new SoftHashTable<Integer, Integer>();
			List<Integer> keys = BasicGenerator.genIntList(M);
			List<Integer> values = BasicGenerator.genIntList(M);
			int nEntries = BasicGenerator.random.nextInt(200) - 100;
			for(int j = 0; j < nEntries; j++) {
				table.put(keys.get(BasicGenerator.random.nextInt(M)), values.get(BasicGenerator.random.nextInt(M)));
			}
			data.add(new DataEntry(keys, values, table));
		}
	}


	@Test
	public final void testClear() {
		for(DataEntry d : data) {
			d.table.clear();
			assertTrue(d.table.isEmpty());
			for(Object o : d.keys) {
				assertNull(d.table.get(o));
			}
		}
	}


	@Test
	public final void testEquals() {
		for(int i = 0; i < M; i++) {
			DataEntry d1 = BasicGenerator.randomElement(data);
			DataEntry d2 = BasicGenerator.randomElement(data);
			DataEntry d3 = BasicGenerator.randomElement(data);

			HashTableAxioms.equalsAxiom(d1.table);
			HashTableAxioms.equalsAxiom(d1.table, d2.table);
			HashTableAxioms.equalsAxiom(d1.table, d2.table, d3.table);
		}
	}


	@Test
	public final void testHashCode() {
		for(DataEntry d1 : data) {
			DataEntry d2 = data.get(BasicGenerator.random.nextInt(data.size()));

			HashTableAxioms.equalsHashAxiom(d1.table, d2.table);
		}
	}


	@Test
	public final void testPutGet1() {
		for(DataEntry d : data) {
			for(int i = 0; i < M; i++) {
				HashTableAxioms.putGetContainsAxiom1(d.table, BasicGenerator.randomElement(d.keys), BasicGenerator.randomElement(d.values));
			}
		}

	}


	@Test
	public final void testPutGet2() {
		for(DataEntry d : data) {
			for(int i = 0; i < M; i++) {
				HashTableAxioms.putGetContainsAxiom2(d.table, BasicGenerator.randomElement(d.keys), BasicGenerator.randomElement(d.keys), BasicGenerator.randomElement(d.values));
			}
		}

	}


	@Test
	public final void testRemove() {
		for(DataEntry d : data) {
			for(int i = 0; i < M; i++) {
				HashTableAxioms.removeAxiom(d.table, BasicGenerator.randomElement(d.keys));
			}
		}
	}


	@Test
	public final void testSize() {
		for(DataEntry d : data) {
			for(int i = 0; i < M; i++) {
				HashTableAxioms.putSizeAxiom(d.table, BasicGenerator.randomElement(d.keys), BasicGenerator.randomElement(d.keys));
			}
		}
	}


	static class DataEntry {
		public final List<Object> keys;
		public final List<Object> values;
		public final SoftHashTable<Object, Object> table;


		@SuppressWarnings("unchecked")
		public DataEntry(List<?> keys, List<?> values, SoftHashTable<?, ?> table) {
			this.keys = (List<Object>) keys;
			this.values = (List<Object>) values;
			this.table = (SoftHashTable<Object, Object>) table;
		}
	}

}
