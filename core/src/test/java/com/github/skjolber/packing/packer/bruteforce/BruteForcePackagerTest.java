package com.github.skjolber.packing.packer.bruteforce;

import static com.github.skjolber.packing.test.assertj.StackablePlacementAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.skjolber.packing.api.Box;
import com.github.skjolber.packing.api.Container;
import com.github.skjolber.packing.api.StackPlacement;
import com.github.skjolber.packing.api.StackableItem;
import com.github.skjolber.packing.impl.ValidatingStack;
import com.github.skjolber.packing.packer.AbstractPackagerTest;
import com.github.skjolber.packing.test.bouwkamp.BouwkampCode;
import com.github.skjolber.packing.test.bouwkamp.BouwkampCodeDirectory;
import com.github.skjolber.packing.test.bouwkamp.BouwkampCodeLine;
import com.github.skjolber.packing.test.bouwkamp.BouwkampCodes;



public class BruteForcePackagerTest extends AbstractPackagerTest {

	@Test
	void testStackingSquaresOnSquare() {

		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("1").withEmptyWeight(1).withSize(3, 1, 1).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());
		
		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();
		
		List<StackableItem> products = new ArrayList<>();

		products.add(new StackableItem(Box.newBuilder().withDescription("A").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("B").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("C").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 1));

		Container fits = packager.pack(products);
		assertValid(fits);
		
		List<StackPlacement> placements = fits.getStack().getPlacements();

		assertThat(placements.get(0)).isAt(0, 0, 0).hasStackableName("A");
		assertThat(placements.get(1)).isAt(1, 0, 0).hasStackableName("B");
		assertThat(placements.get(2)).isAt(2, 0, 0).hasStackableName("C");
		
		assertThat(placements.get(0)).isAlongsideX(placements.get(1));
		assertThat(placements.get(2)).followsAlongsideX(placements.get(1));
		assertThat(placements.get(1)).preceedsAlongsideX(placements.get(2));
	}
	
	@Test
	void testStackMultipleContainers() {

		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("1").withEmptyWeight(1).withSize(3, 1, 1).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());

		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();
		
		List<StackableItem> products = new ArrayList<>();

		products.add(new StackableItem(Box.newBuilder().withDescription("A").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 2));
		products.add(new StackableItem(Box.newBuilder().withDescription("B").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 2));
		products.add(new StackableItem(Box.newBuilder().withDescription("C").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 2));

		List<Container> packList = packager.packList(products, 5, System.currentTimeMillis() + 5000);
		assertValid(packList);
		assertThat(packList).hasSize(2);
		
		Container fits = packList.get(0);
		
		List<StackPlacement> placements = fits.getStack().getPlacements();

		assertThat(placements.get(0)).isAt(0, 0, 0).hasStackableName("A");
		assertThat(placements.get(1)).isAt(1, 0, 0).hasStackableName("A");
		assertThat(placements.get(2)).isAt(2, 0, 0).hasStackableName("B");
		
		assertThat(placements.get(0)).isAlongsideX(placements.get(1));
		assertThat(placements.get(2)).followsAlongsideX(placements.get(1));
		assertThat(placements.get(1)).preceedsAlongsideX(placements.get(2));
	}

	@Test
	void testStackingBinary1() {

		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("1").withEmptyWeight(1).withSize(8, 8, 1).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());

		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();

		List<StackableItem> products = new ArrayList<>();
		products.add(new StackableItem(Box.newBuilder().withDescription("J").withRotate3D().withSize(4, 4, 1).withWeight(1).build(), 1));

		for(int i = 0; i < 4; i++) {
			products.add(new StackableItem(Box.newBuilder().withDescription("K").withRotate3D().withSize(2, 2, 1).withWeight(1).build(), 1));
		}
		for(int i = 0; i < 16; i++) {
			products.add(new StackableItem(Box.newBuilder().withDescription("K").withRotate3D().withSize(1, 1, 1).withWeight(1).build(), 1));
		}

		Container fits = packager.pack(products);
		assertValid(fits);
		assertEquals(products.size(), fits.getStack().getPlacements().size());
	}
	
	@Test
	public void testStackingRectanglesOnSquareRectangleVolumeFirst() {
		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("1").withEmptyWeight(1).withSize(10, 10, 4).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());
		
		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();

		List<StackableItem> products = new ArrayList<>();

		products.add(new StackableItem(Box.newBuilder().withDescription("J").withRotate3D().withSize(5, 10, 4).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("L").withRotate3D().withSize(5, 10, 1).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("J").withRotate3D().withSize(5, 10, 1).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("M").withRotate3D().withSize(5, 10, 1).withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("N").withRotate3D().withSize(5, 10, 1).withWeight(1).build(), 1));

		Container fits = packager.pack(products);
		assertValid(fits);
	}

	@Test
	public void testStackingBox() {
		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("Container").withEmptyWeight(1).withSize(5, 5, 1).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());

		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();

		List<StackableItem> products = new ArrayList<>();

		products.add(new StackableItem(Box.newBuilder().withDescription("A").withSize(3, 2, 1).withRotate3D().withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("B").withSize(3, 2, 1).withRotate3D().withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("C").withSize(3, 2, 1).withRotate3D().withWeight(1).build(), 1));
		products.add(new StackableItem(Box.newBuilder().withDescription("D").withSize(3, 2, 1).withRotate3D().withWeight(1).build(), 1));

		Container fits = packager.pack(products);
		assertValid(fits);
	}
	
	@Test
	public void testSimpleImperfectSquaredRectangles() {
		BouwkampCodeDirectory directory = BouwkampCodeDirectory.getInstance();

		pack(directory.getSimpleImperfectSquaredRectangles(9));
	}
	
	@Test
	public void testSimpleImperfectSquaredSquares() {
		BouwkampCodeDirectory directory = BouwkampCodeDirectory.getInstance();

		pack(directory.getSimpleImperfectSquaredSquares(9));
	}
	
	@Disabled // takes too long
	@Test
	public void testSimplePerfectSquaredRectangles() {
		BouwkampCodeDirectory directory = BouwkampCodeDirectory.getInstance();
		
		pack(directory.getSimplePerfectSquaredRectangles(9));
	}
	
	protected void pack(List<BouwkampCodes> codes) {
		for (BouwkampCodes bouwkampCodes : codes) {
			for (BouwkampCode bouwkampCode : bouwkampCodes.getCodes()) {
				long timestamp = System.currentTimeMillis();
				System.out.println("Package " + bouwkampCode.getName() + " " + bouwkampCodes.getSource());
				pack(bouwkampCode);
				System.out.println("Packaged " + bouwkampCode.getName() + " order " + bouwkampCode.getOrder() + " in " + (System.currentTimeMillis() - timestamp));
			}
		}
	}
	
	protected void pack(BouwkampCode bouwkampCode) {
		List<Container> containers = new ArrayList<>();
		
		containers.add(Container.newBuilder().withDescription("Container").withEmptyWeight(1).withSize(bouwkampCode.getWidth(), bouwkampCode.getDepth(), 1).withMaxLoadWeight(100).withStack(new ValidatingStack()).build());

		BruteForcePackager packager = BruteForcePackager.newBuilder().withContainers(containers).build();

		List<StackableItem> products = new ArrayList<>();

		List<Integer> squares = new ArrayList<>(); 
		for (BouwkampCodeLine bouwkampCodeLine : bouwkampCode.getLines()) {
			squares.addAll(bouwkampCodeLine.getSquares());
		}

		// map similar items to the same stack item - this actually helps a lot
		Map<Integer, Integer> frequencyMap = new TreeMap<>();
		squares.forEach(word ->
        	frequencyMap.merge(word, 1, (v, newV) -> v + newV)
		);
		
		for (Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
			int square = entry.getKey();
			int count = entry.getValue();
			products.add(new StackableItem(Box.newBuilder().withDescription(Integer.toString(square)).withSize(square, square, 1).withRotate3D().withWeight(1).build(), count));
		}

		//Collections.shuffle(products);

		Container fits = packager.pack(products);
		assertNotNull(bouwkampCode.getName(), fits);
		assertValid(fits);
		assertEquals(bouwkampCode.getName(), fits.getStack().getSize(), squares.size());
	}
	
	@Test
	public void issueNew() {
		Container container = Container
			.newBuilder()
			.withDescription("1")
			.withSize(100, 150, 200)
			.withEmptyWeight(0)
			.withMaxLoadWeight(100)
			.build();

		FastBruteForcePackager packager = FastBruteForcePackager
			.newBuilder()
			.withContainers(container)
			.build();

		Container pack = packager.pack(
			Arrays.asList(
				new StackableItem(Box.newBuilder().withId("1").withSize(200, 2, 50).withRotate3D().withWeight(0).build(), 4),
				new StackableItem(Box.newBuilder().withId("2").withSize(1, 1, 1).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("3").withSize(53, 11, 21).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("4").withSize(38, 7, 19).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("5").withSize(15, 3, 7).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("6").withSize(95, 5, 3).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("7").withSize(48, 15, 42).withRotate3D().withWeight(0).build(), 1),
				new StackableItem(Box.newBuilder().withId("8").withSize(140, 10, 10).withRotate3D().withWeight(0).build(), 2),
				new StackableItem(Box.newBuilder().withId("9").withSize(150, 4, 65).withRotate3D().withWeight(0).build(), 2),
				new StackableItem(Box.newBuilder().withId("10").withSize(75, 17, 60).withRotate3D().withWeight(0).build(), 1)
				)
		);

		assertNotNull(pack);
	}

}
