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
			
		},
		
		traverseRidiculousObjectQueryGraphAndDoSomething: function(restrictions, parentRestriction, somethingToDoFunction, somethingElseToDoFunction){
			
			for ( var i = 0; i < restrictions.length; i++) {				
				
				var rest1 = restrictions[i];

				if (rest1.restrictions) {
					
					this.traverseRidiculousObjectQueryGraphAndDoSomething(rest1.restrictions, rest1, somethingToDoFunction, somethingElseToDoFunction);

				} else{	
					
					somethingToDoFunction(rest1, parentRestriction, somethingElseToDoFunction);				
					
					//Disjunction Junction, what's your Function?
					if (parentRestriction instanceof Disjunction) return;
				}

			}
			
		}
		
};
