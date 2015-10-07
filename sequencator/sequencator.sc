Sequencator {

	var fenetre;
	var suitedAccords;
	var layoutParties, layoutAccords;
	var parties, accords;
	var partiesArray, accordsArray;
	var boutonsSup; //ça c'est ceux qu'on rajoute, bravo la prog a deux balles...

	*new{
		// arg;
		^super.new.init
	}

	init {

		//juste des helpers

		var compressArray={
			arg arr, threshold, function;

			if(threshold.isArray){threshold=threshold.maxItem};
			arr.flatten.collect{
				arg i;
				if (i>threshold)
				{i=i.perform(function, 1)}
				{i=i}

			}.reshapeLike(arr)
		};

		var rajouterBoutons={
			accordsArray=accordsArray.add(suitedAccords);
			accords.add(accordsArray.last.layout);
			boutonsSup[1]=boutonsSup[1].add(
				((fenetre.asView.children.lastIndex) ..
					((fenetre.asView.children.lastIndex)-accordsArray.last.boutons.size+1)
				).reverse// trop LAID !!
			); // on incrémente
			boutonsSup[1].postln
		};
		var enleverBoutons={
			if (boutonsSup[1].isEmpty.not)
			{
				fenetre.asView.children[boutonsSup[1].last].collect(_.destroy);
				boutonsSup[1]=boutonsSup[1].drop(-1);
				boutonsSup[1].postln;
			}
		};

		parties=HLayout(); accords=VLayout();
		boutonsSup=[[], []];
		fenetre=Window().front;

		// les boutons

		suitedAccords=
		(
			boutons:
			{	arg self;
				[
					PopUpMenu().items_([
						"dominante ->",
						"tonique ->"
					]),
					PopUpMenu().items_([
						"dominante",
						"tonique"
					]),
					StaticText().string_("durees : "),
					NumberBox().value_(1).clipLo_(1),
					StaticText().string_("edit?"),
					CheckBox().action_{
						arg s;
						switch(s.value,
							true, {self.box.front},
							false, {self.box.quit}
						);
					}
				];
			},
			layout: {|self| HLayout(*self.boutons) },
			box: ChordMatrix(),
		);

		layoutParties=
		VLayout(
			parties,
			HLayout(
				Button()
				.states_(
					[["ajouter une partie?"]]
				).action_{
					partiesArray=partiesArray.add(Button());
					parties.add(partiesArray.last);
					boutonsSup[0]=boutonsSup[0].add(fenetre.asView.children.lastIndex); // on poute le grozindékse des enfants de double vé
					rajouterBoutons.();
				},
				Button()
				.states_(
					[["enlever une partie?"]]
				)
				.action_{
					if (boutonsSup[0].isEmpty.not)
					{
						fenetre.asView.children[boutonsSup[0].last].destroy;
						//moment casse couilles de gestion de children
						boutonsSup[1].postln;
						boutonsSup[0]=boutonsSup[0].drop(-1);
						enleverBoutons.();
					}
				}
		));


		// les Layouts
		layoutAccords=
		VLayout(
			accords);


		fenetre.layout_((
			HLayout(
				layoutParties,
				layoutAccords
			)
		)
		);

	}
}