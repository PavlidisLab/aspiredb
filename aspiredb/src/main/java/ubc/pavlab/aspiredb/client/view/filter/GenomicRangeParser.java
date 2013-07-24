/*
 * The aspiredb project
 * 
 * Copyright (c) 2012 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubc.pavlab.aspiredb.client.view.filter;


import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Genomic range parser
 * 
 * @author frances
 * @version $Id: GenomicRangeParser.java,v 1.2 2013/04/19 06:51:20 frances Exp $
 */
public class GenomicRangeParser {
    public static enum State {INITIAL, CHROMOSOME, START_RANGE, END_RANGE, COORDINATE}

	private static final String CHROMOSOME_PATTERN = "(1\\d|2[0-2]|[xy]|[1-9])";
	private static final String BAND_PATTERN = "([pq]\\d+(\\.\\d+)?)";
	private static final String COORDINATE_PATTERN = "(\\d{1,9})\\-(\\d{1,9})";
	
	private static final int[] CHROMOSOME_INDICES = { 2, 8, 12, 15 };
	private static final int[] START_BAND_INDICES = { 3, 13 };
	private static final int[] END_BAND_INDICES = { 5 };
	private static final int[] START_BASE_INDICES = { 9 };
	private static final int[] END_BASE_INDICES = { 10 };
	
	// Note: if it is changed, all of the above indices AND the indices used in parse() must be changed as well. 
	private static final String PATTERN =
				"(" + CHROMOSOME_PATTERN + BAND_PATTERN + "\\-" + BAND_PATTERN + ")"
		+ "|" + "(" + CHROMOSOME_PATTERN + "\\:" + COORDINATE_PATTERN + ")"
		+ "|" + "(" + CHROMOSOME_PATTERN + BAND_PATTERN + ")"
		+ "|" + CHROMOSOME_PATTERN;


	private static final RegExp REG_EXP = RegExp.compile(PATTERN, "i"); // ignore case
		

	public static class ParseResult {
		private MatchResult matchResult;
		private State state;
		private boolean isValid;

		private ParseResult(MatchResult matchResult, State state, boolean isValid) {
			this.matchResult = matchResult;
			this.state = state;
			this.isValid = isValid;
		}
		
		private String getMatchedGroupByIndices(int[] indices) {
			for (int index: indices) {
				String group = this.matchResult.getGroup(index);
				
				if (group != null) {
					return group;
				}
			}
			return null;
		}

		public State getState() {
			return this.state;
		}
		
		public String getChromosome() {
			return getMatchedGroupByIndices(CHROMOSOME_INDICES);
		}

		public String getStartBand() {
			return getMatchedGroupByIndices(START_BAND_INDICES);
		}

		public String getEndBand() {
			return getMatchedGroupByIndices(END_BAND_INDICES);
		}
		
		public int getStartBase() {
			String startBase = getMatchedGroupByIndices(START_BASE_INDICES);

			return startBase == null ? -1 : Integer.valueOf(startBase);
		}
		
		public int getEndBase() {
			String endBase = getMatchedGroupByIndices(END_BASE_INDICES);

			return endBase == null ? -1 : Integer.valueOf(endBase);
		}
		
		public boolean isBand() {
			return isValid && getMatchedGroupByIndices(START_BAND_INDICES) != null;
		}
		
		public boolean isBase() {
			return isValid && getMatchedGroupByIndices(START_BASE_INDICES) != null && getMatchedGroupByIndices(END_BASE_INDICES) != null;
		}

		public boolean isValid() {
			return isValid;
		}
	}
	
	// private constructor
	private GenomicRangeParser() {}
	
	public static ParseResult parse(String query) {
		String queryInLowerCase = query.toLowerCase(); 
		
        MatchResult matchResult = REG_EXP.exec(queryInLowerCase);
        
        final ParseResult parseResult;
        
        if (matchResult == null) {
        	parseResult = null;
        } else {
	        final State state;
			if (queryInLowerCase.contains(":")) {
			    state = State.COORDINATE;
			} else if ((matchResult.getGroup(12) != null && (queryInLowerCase.endsWith("-") || queryInLowerCase.endsWith("p") || queryInLowerCase.endsWith("q")))
					|| matchResult.getGroup(2) != null) {
	            state = State.END_RANGE;
	        } else if ((matchResult.getGroup(15) != null && (queryInLowerCase.endsWith("p") || queryInLowerCase.endsWith("q")))
	        		|| matchResult.getGroup(12) != null) {
	        	state = State.START_RANGE;
	        } else if ( matchResult.getGroup(15) != null ) {
	        	state = State.CHROMOSOME;
	        } else {
	        	state = State.INITIAL;
	        }
	        
	        parseResult = new ParseResult(matchResult, state, matchResult.getGroup(0).length() == query.length());
        }
        
        return parseResult;
	}
}
