/*
 * The aspiredb project
 * 
 * Copyright (c) 2013 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var FilterUtil = {		
		 
		//I thought that all these nested for loops was ridiculous until I looked at the old gwt implementation and saw the same thing.
		//Putting this restriction query object navigation code in its own file since we need to call it a couple times.
		//Recursion would probably make things more elegant yet harder to debug however the limit of the 
		//complexity of the queries is literally hard coded into the application so its a moot point
		traverseRidiculousObjectQueryGraphAndDoSomething: function(restriction, somethingToDoFunction, somethingElseToDoFunction){
			
			for ( var i = 0; i < restriction.restrictions.length; i++) {

				
				//Conjunction
				var rest1 = restriction.restrictions[i];

				if (rest1.restrictions) {

					var rest1Array = rest1.restrictions;

					for ( var j = 0; j < rest1Array.length; j++) {

						//Disjunction
						rest2 = rest1Array[j];

						if (rest2.restrictions) {

							var rest2Array = rest2.restrictions;

							for ( var k = 0; k < rest2Array.length; k++) {
								//Specific Restriction
								var rest3 = rest2Array[k];								
								
								somethingToDoFunction(rest3, rest2, somethingElseToDoFunction);
							}

						} else {							
							somethingToDoFunction(rest2, rest1, somethingElseToDoFunction);							
						}

					}

				} else{
					//this probably will never get called
					alert("FilterUtil: unexpected Object");
					somethingToDoFunction(rest1, rest1, somethingElseToDoFunction);
				}

			}
			
		},

		//Simple means not Disjunction or Conjunction
		validateSimpleRestriction: function(restriction){
			
			if (restriction instanceof PhenotypeRestriction && restriction.name && restriction.value){
				
				return true;
				
			}else if (restriction instanceof SetRestriction && restriction.operator && restriction.property && restriction.values && restriction.values.length>0){
			
				return true;
			
			}
			else if (restriction instanceof SimpleRestriction && restriction.operator && restriction.property && restriction.value && restriction.value.value !==''){
				
				return true;
			}
			
			return false;
			
		},
		
		isSimpleRestriction : function(restriction){
			
			if (restriction instanceof PhenotypeRestriction || restriction instanceof SetRestriction ||restriction instanceof SimpleRestriction){
				return true;
			}
			
			return false;
			
		}
		
};
