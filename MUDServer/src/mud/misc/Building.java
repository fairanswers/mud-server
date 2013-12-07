package mud.misc;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.awt.Point;
import java.util.List;
import java.util.Map;

public class Building {
	private String name;
	private String shortName;
	
	private Map entrances; // places where you can enter the building (ex. doors, windows, holes in the walls)0
	private Map exits;     // places where you can leave the building (ex. doors, windows, holes in the walls)0
	
	private List<Edge> sides;
	
	public Building(String name, String shortName) {
		
	}
	
	private class Edge {
		private Point start;
		private Point end;
		private int length;
		
		public Edge(Point startP, Point endP, int length) {
			this.start = startP;
			this.end = endP;
			this.length = length;
		}
		
		public Point getStartPoint() {
			return start;
		}
		
		public Point getEndPoint() {
			return end;
		}
		
		public int length() {
			return this.length;
		}
	}
}