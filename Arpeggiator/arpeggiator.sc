/*
(
//init basique

s.boot;
a=Arpeggiator(\test);
a.front;

Arpeggiator.t.stop
Pdef(\test).stop
Pdefn(\grille).source.postcs
Pdefn(\formuleAcc).source.postcs
		Pdefn(\root, 0);
		Pdefn(\amp, 0.82);


// mettre dans une nouvelle fenêtre
a.newWindow
w=Window().front
a.newWindow(w)

Pdefn(\formuleAcc).source.list.postcs;
Pdef(\test).trace.play
)

*/


Arpeggiator
{

	var <name, <>fenetre, <grille;
	var arg_passe; // ça c'est un petit trick pour l'elegance'

	*new{
		arg name, fenetre=Window(), grille=#[[0, 2, 4]];
		^super.newCopyArgs(name, fenetre, grille).init
	}

	init{
		//


		var msv, slider;
		var valueMSV= 4 !4;
		var lastValSlider=0.1;
		var ctlSpecMSV=ControlSpec(0, 10, 'lin', 1);
		var t=TempoClock((1/valueMSV).reciprocal);
		var index=(-1);
		var pdefPlaying;



		var daemon=TempoClock.play({

			valueMSV.postln;
			index.postln;
			1;

		});


		var		layout=
		VLayout
		(
			HLayout(
				StaticText()
				.string_(grille.asString),
/*				StaticText()
				.string_(valueMSV)*/
			),
			HLayout(
				msv=MultiSliderView()
				.elasticMode_(true)
				.showIndex_(false)
				.step_(1/ctlSpecMSV.maxval)
				.value_(0.5 ! 4)
				.action_{
					arg self;
					var selfVal=ctlSpecMSV
					.map(self.value);
					if(self.index >= valueMSV.size)
					{}
					{
						valueMSV[self.index]=selfVal[self.index];
					};
				}
				.mouseDownAction_({
					arg self;
					self.showIndex_(false)
				})
				.mouseUpAction_({
					arg self;
					self.showIndex_(true)
				}),
				slider=Slider()
				.value_(lastValSlider)
				.orientation_(\vertical)
				.action_{
					arg self;
					var val=ControlSpec(1, 32, 'lin', 1)
					.map(self.value);
					if (val > valueMSV.size)
					{
						while{val>(valueMSV.size)}
						{valueMSV=valueMSV.add(4)}
					}
					{
						if (val < (valueMSV.size))
						{
							while
							{val<(valueMSV.size)}
							{valueMSV=valueMSV.drop(-1)}
						}
						{}
					};
					msv.valueAction_(ctlSpecMSV.unmap(valueMSV));
				}
			),
			Button()
			.states_([
				["play?"],
				["stop?"]
			])
			.action_{
				arg self;

				switch(self.value,
					1, {
						r{
							{
								msv.showIndex_(true);
							}.defer(0);

							inf.do{

								index=index+1;
								if(index >= (valueMSV.size))
								{
									"swap".postln;
									index=0
								};
								AppClock.sched(0,
									{
										("index"+index).postln;
										msv.index_(index);
									}
								);
								1.wait;}
						}.play(t, quant:1);

						 pdefPlaying=Pdef(name.asSymbol).play(t, quant:1);
					},
					0, {
						pdefPlaying.stop;
						msv.showIndex_(false);
						t.clear;
						index=(-1)
					},
				)
			}
		)	;

		/// LES PDEFS ///


		Pdefn(\grille,
			Pseq(grille, inf)
		);
		Pdefn(\formuleAcc,
			Pn(	Plazy
				(
					{valueMSV[index]})
			)
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
					accord@index;
				}),

				\dur, 1,
			)
		);

		fenetre.layout_(layout)
	}

	newWindow{
		arg niouFen=(arg_passe=false; Window("copie"));
		if(arg_passe)
		{
			fenetre=CompositeView(niouFen);
			this.init;
		}
		{
			this.fenetre=niouFen;
			this.init.front;
		}

	}

	front{

		if(fenetre.class==Window)
		{fenetre.front;
			^fenetre.visible}
		{
			("votre fenetre étant une : ")+fenetre.class+
			("elle n'a pas de méthode front")
		};


	}

}


