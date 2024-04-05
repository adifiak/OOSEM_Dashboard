package te {
	classifier ToExecute {
		feature a : Cl [1];
		
		feature b1 : Cl [*];
		feature b2 : Cl [*];
		connector b : Co from b1 [1] to b2 [1];
		
		feature odd unions a, b1, b2;
		
		feature c1 : Cl [*];
		feature c2 : Cl [*];
		connector c : Co from c1 [1] to c2 [1];
		feature even unions c1, c2;
	}
	
	classifier Cl;
	assoc Co {
		end feature one [1] : Cl;
		end feature other [1] : Cl;
		
	}
}