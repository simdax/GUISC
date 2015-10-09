/*
(
s.boot;
a=Arpeggiator("test");
a.front;


Pdefn(\formuleAcc).source.list.postcs;
Pdef(\test).trace.play
w=
a=Arpeggiator("test").front;
w=Window().front
a.fenetre.asView.setParents(w)
)

*/


Arpeggiator
{

	var name, <>fenetre, grille;

	*new{
		arg name, fenetre=Window(), grille=#[[0, 2, 4]];
		^super.newCopyArgs(name, fenetre, grille).init
	}

	init{
		//
		var msv, slider;
		var ancienIndex=0;
		var valueMSV= 4 !4;
		var lastValSlider=0.1;

		var		layout=
		VLayout
		(
			HLayout(
				msv=MultiSliderView()
				.elasticMode_(true)
				.showIndex_(false)
				.value_(0.5 ! 4)
				.action_{
					arg self;
					var a=ControlSpec(
						0, 8, 'lin', 1)
					.map(self.value);
					valueMSV[self.index]=a[self.index];
				},
				slider=Slider()
				.value_(lastValSlider)
				.orientation_(\vertical)
				.action_{
					arg self;
					var val=ControlSpec(1, 32, 'lin', 1)
					.map(self.value);
					if (val > valueMSV.size)
					{
						{valueMSV=valueMSV.add(4)}
						.while(val>valueMSV.size)
					}
					{t b
						if (val < valueMSV.size)
							{
							{valueMSV=tvalueMSV.drop(-1)}
							.while(val<valueMSV.size)
							}
							{}
					};
					msv.valueAction_(valueMSV);
				}
			),
			Button()
			.states_([
				["play?"],
				["stop?"]
			])
			.action_{
				arg self;
				var t2;
				switch(self.value,
					1, {
						msv.showIndex_(true);
						t2=TempoClock(2);
						t2.play({
							var index, beats;
							beats=t2.beats.ceil;
							index=beats%valueMSV.size;

							if(index != (ancienIndex+1) and:
								index !=0
							)
							{"normal".postln;
								index=ancienIndex+1};
							if(index >= valueMSV.size)
							{"swap".postln;
								index=0};

							AppClock.sched(0,
								{
									msv.index_(index)
								}
							);
							ancienIndex=index;
							1;
						});
						Pdef(name.asSymbol).play(t2, quant:1);
					},
					0, {
						Pdef(name.asSymbol).stop;
						msv.showIndex_(false);
						t2.clear; t2.stop;
					},
				)
			}
		)	;

		/// LES PDEFS ///


		Pdefn(\grille,
			Pseq(grille, inf)
		);
		Pdefn(\formuleAcc,
			Pseq(valueMSV, inf)
		);

		Pdefn(\forme, Pseq([
			( 4 ! 4 ) ! 4
		].flat)
		);
		Pdefn(\root, 0);
		Pdefn(\amp, 0.82);


		Pdef(name.asSymbol,
			Pbind(
				// \instrument, \a,

				\root, Pdefn(\root),
				\amp, Pdefn(\amp),
				\legato, 0.95,

				\grille, Pdefn(\grille),

				\arpeggiator, Pdefn(\formuleAcc),
				\octave, 3 + (Pkey(\arpeggiator) div: 3),
				\degree, Pfunc({ |ev|
					var accord=ev.grille;
					var index=ev.arpeggiator %3;
					accord.at(index);
				}),

				\dur, 1,
			)
		);

		fenetre.layout_(layout)
	}

	newWindow{
		arg niouFen=Window("copie");
		this.fenetre=niouFen
	}

	front{
		if(fenetre.class==Window)
		{fenetre.front;
			^fenetre.visible}
		{
			("votre fenetre étant une : ")+fenetre.class+
			("elle n'a pas de méthode front")
		}
	}

}

