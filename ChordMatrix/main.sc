// petit fichier pour générer une matrice de boutons
// qui représente une suite d'accords

/*
TODO

0. bug pour une mélodie de 1 note

1. rendre non editable les boutons mis à zero

2. harmoniser selon d'autres règles

3. rendre automatique le changement, sans repasser par un appel de init

*/


ChordMatrix{

	var <pattern;
	var fenetre, fenMatrice, fenBoutons;
	var <sliders, <boutons,
	<>couleurs, <>basses;

	*new{
		arg pattern=#[0,0];
		^super.newCopyArgs(pattern).init.draw
	}

	init{
		fenetre=Window();
		fenetre.addFlowLayout;
		fenMatrice=CompositeView(fenetre, fenetre.bounds.width*1.25 @ fenetre.bounds.height*0.8)
		.background_(Color.rand);
		fenBoutons=CompositeView(fenetre, fenetre.bounds.width @ fenetre.bounds.height*0.15)
		.background_(Color.rand);
		fenMatrice.addFlowLayout;
	}

	pattern_{
		arg pat;
		this.draw;
		^pattern=pat;
	}

	draw{
		var tailleMelodie=pattern.size;

		//// variables graphiques

		var largeurPrincipale= fenMatrice.bounds.width;
		var hauteurPrincipale= fenMatrice.bounds.height;
		var tailleBoutons=(
					( (largeurPrincipale)  / (tailleMelodie+1) / 2 )
					@
					(hauteurPrincipale/8)
		) ;

		/// fonction pour drawer les sliders
		// TODO fix le truc de normalizeSum

		var updateSlider={ |a|
			var v=a.collect(_.value).normalizeSum(1);
			v.collect({ |i, index|
				a[index].value_(i)
			})
		};

		/// setting des boutons

		boutons=["do", "re", "mi", "fa", "sol", "la", "si"].reverse;

		//ACTIONS
		// on nettoie tout

		fenMatrice.removeAll;
		fenMatrice.decorator.reset;

		boutons=boutons.collect({ |i|
			var x;

			// les premiers boutons pour les notes
			Button(fenMatrice, tailleBoutons)
			.states_([
				[i.asString, Color.red, Color.black]
			]);

			x=Array.fill(tailleMelodie, { |index|
				[
					Button(fenMatrice, tailleBoutons)
					.states_([
						['0', Color.green, Color.yellow],
						['5', Color.yellow, Color.red],
						['6', Color.yellow, Color.red],
						['7', Color.yellow, Color.red]
					])
					.action_({
						// this.draw;
					})
					,
					Slider(fenMatrice, tailleBoutons)
					.orientation_(\vertical)
				]
			});
			fenMatrice.decorator.nextLine;
			x.flop;
		}).unbubble(2);


		// on sépare les sliders et les boutons

		sliders=
		tailleMelodie.collect({ |i|
			boutons.collect({ |j|
				j[1][i]
			})
		});
		boutons=
		tailleMelodie.collect({ |i|
			boutons.collect({ |j|
				j[0][i]
			})
		});

		// on rajoute une fonction aux sliders :

		sliders.do({ |i|
			i.collect({ |j|
				j.action_({
					updateSlider.(i)
				})
			})
		});


		Button(fenBoutons, 50@50)
		.states_([
			["harmoniser"]
		])
		.action_({
			this.harmoniser
		});

	}


	reinit { | value=0|
			// on remet les boutons à 0
			boutons.deepCollect(2, { |i|
				i.value_(value)
			});
			// on remet les sliders à 0
			sliders.deepCollect(2, { |i|
				i.value_(0)
			})
	}



	/////////// ANALYSE

	analyserMel {
		var structure=Harmonie.troisAccords;
		^Harmonie.analyseHarmonique(
			pattern,
			structure.choose,
			false);
	}

	// fonction pour changer ce qui doit être harmonisé par rapport à la melodie

	harmoniser {

		var bas=this.analyserMel;

		// on change les valeurs pour inverser le truc
		basses= bas.deepCollect(2,{ |i|
			switch(i,
				0,6,
				1,5,
				2,4,
				3,3,
				-3,2,
				-2,1,
				-1,0
			)
		});

		this.reinit(0);

		// on change les boutons comme il faut
		basses.collect({ |item, index|
			item.collect({ |i|
				boutons[index][i]
				.value_(1)
			})
		});
		// on change les sliders
		basses.collect({ |item, index|
			item.collect({ |i|
				sliders[index][i]
				.valueAction_(0.5)
			})
		});

		//on définit la variable couleur
		couleurs=basses.collect({ |i, index|
			i.collect({ |j|
				switch(boutons[index][j].value,
					0, [0,2,4],
					1, [0,2,5],
					2, [0,2,4,6]
				)
			})
		});


	}

	front{
		^fenetre.visible;
	}
	quit{
		fenetre.close;
		^fenetre.isClosed;
	}


}