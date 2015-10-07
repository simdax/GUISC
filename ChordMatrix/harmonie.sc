// interface pour les calculs relevant de l''harmonique


Harmonie{


	//// fonctions pour transformer une mélodie en ensemble de note sans souci d'intervalles

	// pour une note
	*reduireHarmo{ |n, modulo=7|
		var note=n%7;
		^(note < (modulo/2)).if ({note}, { note - modulo });
	}

	// pour toute une mélodie
	*convertirMel{ |mels|
		var temp;
		temp=mels.flatten(mels.rank);
		temp=temp.collect(
			this.reduireHarmo(_)
		);
		^temp.reshapeLike(mels);
	}

	// permet de créer des ensembles de voix à partir d'une suite d'accords
	//  chaque voix représentant un parcourds parmi les possibilités harmoniques

	*differencierVoix { |accords|
		^accords.collect( ~allPerm.(_))
		.allTuples
		.collect(_.flop);
	}


	//// pour classer les consonnances ??

	*sortSolfege { |mel, ordreGrandeur=#[0,1, 2, 3, -3, -2, -1]|
		^this.convertirMel(mel).sort({ |a, b|
			ordreGrandeur.indexOf(a)
			< 		ordreGrandeur.indexOf(b)
		})
	}


	// permet de définir une grille par rapport à une basse harmo
	// de base, definit juste trois accords majeurs, do fa sol
	// TODO : faire en sorte que la structure se répètent aussi
	// pour s'accorder avec les basses, et pouvoir créer des grilles
	// de style jazzy

	//(type est pour définir des structures de clusters ou quartes

	*structureHarmonique  { | accords=#[0,3,-3], structure=3, type=2 |
		^accords.collect(
			Array.series(structure, _, type)
		);
	}

	*troisAccords { |ml=#[0,1,2,3,4,5,6], modulo=((-3..3))|
		var mil, accords, resultat;
		mil=this.convertirMel(ml);
		accords=all{
			: Set[x,y,z],

			x <- modulo,
			y <- modulo,
			z <- modulo,

			var mel=mil.as(Set),
			var a=this.convertirMel(Array.series(3, x, 2)).as(Set),
			var b=this.convertirMel(Array.series(3, y, 2)).as(Set),
			var c=this.convertirMel(Array.series(3, z, 2)).as(Set),
			var d=  (a | b | c),
			// var mel=this.convertirMel(ml),
			// ::d.postln,
			// ::mel.postln,
			mel == d
		};
		resultat=accords.species.new(accords.size);
		accords.collect({|i|
			if ( resultat.collect(_ == i).includes(true) ,
				{},
				{resultat.add(i)}
			)
		});
		^resultat.collect(_.as(Array))
		.collect(_+.t [0,2,4]);
	}

	//FIX !!

	*quatreAccords { |ml=((..12)), modulo=((-6..6))|
		var mil, accords, resultat;
		mil=(ml);
		accords=all{
			: Set[x,y,z, w],

			x <- modulo,
			y <- modulo,
			z <- modulo,
			w <- modulo,

			var mel=mil.as(Set),
			var a=(Array.series(4, x, 2)).as(Set),
			var b=(Array.series(4, y, 2)).as(Set),
			var c=(Array.series(4, z, 2)).as(Set),
			var e=(Array.series(4, w, 2)).as(Set),
			var d=  (a | b | c | e),

			::d.postln,
			::mel.postln,
			mel == d
		};
		resultat=accords.species.new(accords.size);
		accords.collect({|i|
			if ( resultat.collect(_ == i).includes(true) ,
				{},
				{resultat.add(i)}
			)
		});
		^resultat.collect(_.as(Array))
		.collect(_+.t [0,2,4,6]);
	}

	*analyseHarmonique { |mel,structAccords=(this.structureHarmonique), view=false|
		var result=
		this.convertirMel(mel)
		.collect({ |i|
			this.convertirMel(structAccords).collect({ |j|
				j.includes(i).if
				(j.first)
			})
		})
		.collect( _.reject(_.isNil) );
		if ( view, {~visualiserAnalyse.(result)}) ;
		^result
	}



	///// fonction principale
	//// permet de classifier des lignes de basse
	/// pour l'instant sert pour ainsi dire à rien
	///// => checker filtrer Vecteurs

	*trouverGrille { |analyse|
		var m=analyse.allTuples;

		m=m.at ({
			var a=m.collect(~trouverPattern.(_));
			var index=a.collect(_.size).maxIndex;
			// ("on a ces "++a[index].size++" patterns : "++a[index]).postln;
			index
		}.value);

		("grille générée : "++m).postln;

		^m

	}




	//retourne l'index d'une note prises entre deux valeurs similaires
	*trouverMvtPendulaire { |array|
		var nbNiveau=array.size-1;
		var inter={ |x| x.differentiate.drop(1) };
		var x=inter.(array) ;
		var arr=List.new;
		x.doAdjacentPairs({ arg a, b;
			(arr).add(a+b)
		});
		^arr.collect({ |i, index|
			(i==0).if (
				index+1)
		})
		.reject(_.isNil)
		.select({ |x|
			array[x] != array[x+1]
		})
	}


	*ignorerPendules { |vecteurs|
		var x=~trouverMvtPendulaire.(vecteurs);
		var y=(x++(x+1));
		^vecteurs.reject({ |i, index|
			y
			.includes(index)
		})
	}

	*vecteurs {  |basses, tolerance=1|
		^this.convertirMel(
			basses
			.collect({ |x|
				var y=~trouverMvtPendulaire.(x);
				x
				.differentiate
				.drop(1);
			})
		);
	}


	*nbVecteur { |vecteurs, tendance=1|
		var dom =~ignorerPendules.(vecteurs).sign.occurrencesOf(tendance);
		^dom.size;
	}

	*filtrerVecteur { |basses|
		^basses.at(
			~vecteurs.(basses)
			.maxIndex( ~nbVecteur.(_) )
		)
	}



}