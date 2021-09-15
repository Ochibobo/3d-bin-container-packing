package com.github.skjolber.packing.points;

import java.util.List;

import com.github.skjolber.packing.test.BouwkampCode;
import com.github.skjolber.packing.test.BouwkampCodeLine;

public class BouwkampConverter {

	public DefaultExtremePoints2D convert(BouwkampCode bkpLine, int factor) {
		DefaultExtremePoints2D points = new DefaultExtremePoints2D(bkpLine.getWidth() * factor, bkpLine.getDepth() * factor); 
		
		List<BouwkampCodeLine> lines = bkpLine.getLines();

		int count = 0;

		for(BouwkampCodeLine line : lines) {
			
			List<Integer> squares = line.getSquares();
		
			int minY = points.getMinY();
			
			Point2D value = points.getValue(minY);
			
			int offset = value.getMinX();
			
			int nextY = minY;
			
			
			for(int square : squares) {
				System.out.println("Add " + offset + "x" + value.getMinY() + " " + square + "x" + square + " " + count);
				
				int factoredSquare = factor * square;
				
				points.add(nextY, new DefaultPlacement2D(offset, value.getMinY(), offset + factoredSquare, value.getMinY() + factoredSquare));
	
				offset += factoredSquare;
	
				nextY = points.get(offset, value.getMinY());

				count++;
				
				if(count > 4) {
					return points;
				}
			}


			
		}
		return points;
	}
}