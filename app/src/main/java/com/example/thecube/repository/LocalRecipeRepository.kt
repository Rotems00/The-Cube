package com.example.thecube.repository

import com.example.thecube.model.Dish

object LocalRecipeRepository {

    // Hardcoded list of dishes with the exact country names.
    private val dishes = listOf(
        // United States
        Dish(
            id = "us1",
            dishName = "Cheeseburger",
            dishDescription = "Juicy cheeseburger with all the fixings.",
            imageUrl = "https://example.com/images/cheeseburger.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 150,
            ingredients = "Beef, bun, cheese, lettuce, tomato",
            country = "United States"
        ),
        Dish(
            id = "us2",
            dishName = "Hot Dog",
            dishDescription = "Classic American hot dog.",
            imageUrl = "https://example.com/images/hotdog.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 120,
            ingredients = "Sausage, bun, mustard, ketchup",
            country = "United States"
        ),
        Dish(
            id = "us3",
            dishName = "Fried Chicken",
            dishDescription = "Crispy fried chicken.",
            imageUrl = "https://example.com/images/friedchicken.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 200,
            ingredients = "Chicken, flour, spices",
            country = "United States"
        ),
        Dish(
            id = "us4",
            dishName = "BBQ Ribs",
            dishDescription = "Slow-cooked BBQ pork ribs.",
            imageUrl = "https://example.com/images/bbqribs.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 180,
            ingredients = "Pork ribs, BBQ sauce, spices",
            country = "United States"
        ),
        Dish(
            id = "us5",
            dishName = "Apple Pie",
            dishDescription = "Traditional apple pie.",
            imageUrl = "https://example.com/images/applepie.jpg",
            flagImageUrl = "https://flagcdn.com/w320/us.png",
            countLikes = 90,
            ingredients = "Apples, pie crust, cinnamon, sugar",
            country = "United States"
        ),

        // United Kingdom
        Dish(
            id = "uk1",
            dishName = "Fish and Chips",
            dishDescription = "Battered fish with chips.",
            imageUrl = "https://example.com/images/fishandchips.jpg",
            flagImageUrl = "https://flagcdn.com/w320/gb.png",
            countLikes = 80,
            ingredients = "Fish, potatoes, batter, tartar sauce",
            country = "United Kingdom"
        ),
        Dish(
            id = "uk2",
            dishName = "Shepherd's Pie",
            dishDescription = "Hearty lamb and vegetable pie with mashed potato topping.",
            imageUrl = "https://example.com/images/shepherdspie.jpg",
            flagImageUrl = "https://flagcdn.com/w320/gb.png",
            countLikes = 70,
            ingredients = "Lamb, vegetables, mashed potatoes",
            country = "United Kingdom"
        ),
        Dish(
            id = "uk3",
            dishName = "Roast Beef",
            dishDescription = "Traditional roast beef dinner.",
            imageUrl = "https://example.com/images/roastbeef.jpg",
            flagImageUrl = "https://flagcdn.com/w320/gb.png",
            countLikes = 85,
            ingredients = "Beef, roast potatoes, vegetables, gravy",
            country = "United Kingdom"
        ),
        Dish(
            id = "uk4",
            dishName = "Bangers and Mash",
            dishDescription = "Sausages with mashed potatoes.",
            imageUrl = "https://example.com/images/bangersandmash.jpg",
            flagImageUrl = "https://flagcdn.com/w320/gb.png",
            countLikes = 60,
            ingredients = "Sausages, mashed potatoes, onion gravy",
            country = "United Kingdom"
        ),
        Dish(
            id = "uk5",
            dishName = "Full English Breakfast",
            dishDescription = "Hearty breakfast with eggs, bacon, sausage, and more.",
            imageUrl = "https://example.com/images/englishbreakfast.jpg",
            flagImageUrl = "https://flagcdn.com/w320/gb.png",
            countLikes = 95,
            ingredients = "Eggs, bacon, sausage, baked beans, toast",
            country = "United Kingdom"
        ),

        // France
        Dish(
            id = "fr1",
            dishName = "Croissant",
            dishDescription = "Flaky, buttery croissant.",
            imageUrl = "https://example.com/images/croissant.jpg",
            flagImageUrl = "https://flagcdn.com/w320/fr.png",
            countLikes = 110,
            ingredients = "Flour, butter, yeast, sugar",
            country = "France"
        ),
        Dish(
            id = "fr2",
            dishName = "Coq au Vin",
            dishDescription = "Chicken braised in red wine with mushrooms and onions.",
            imageUrl = "https://example.com/images/coqauvin.jpg",
            flagImageUrl = "https://flagcdn.com/w320/fr.png",
            countLikes = 130,
            ingredients = "Chicken, red wine, mushrooms, onions, garlic",
            country = "France"
        ),
        Dish(
            id = "fr3",
            dishName = "Ratatouille",
            dishDescription = "Stewed vegetable dish from Provence.",
            imageUrl = "https://example.com/images/ratatouille.jpg",
            flagImageUrl = "https://flagcdn.com/w320/fr.png",
            countLikes = 75,
            ingredients = "Eggplant, zucchini, bell peppers, tomatoes",
            country = "France"
        ),
        Dish(
            id = "fr4",
            dishName = "Quiche Lorraine",
            dishDescription = "Savory pie with bacon and cheese.",
            imageUrl = "https://example.com/images/quiche.jpg",
            flagImageUrl = "https://flagcdn.com/w320/fr.png",
            countLikes = 90,
            ingredients = "Eggs, cream, bacon, cheese, pastry",
            country = "France"
        ),
        Dish(
            id = "fr5",
            dishName = "Bouillabaisse",
            dishDescription = "Traditional fish stew from Marseille.",
            imageUrl = "https://example.com/images/bouillabaisse.jpg",
            flagImageUrl = "https://flagcdn.com/w320/fr.png",
            countLikes = 85,
            ingredients = "Fish, shellfish, tomatoes, saffron, herbs",
            country = "France"
        ),

        // Italy
        Dish(
            id = "it1",
            dishName = "Spaghetti Carbonara",
            dishDescription = "Classic pasta with eggs, pancetta, and Parmesan.",
            imageUrl = "https://example.com/images/carbonara.jpg",
            flagImageUrl = "https://flagcdn.com/w320/it.png",
            countLikes = 100,
            ingredients = "Spaghetti, eggs, pancetta, Parmesan, black pepper",
            country = "Italy"
        ),
        Dish(
            id = "it2",
            dishName = "Margherita Pizza",
            dishDescription = "Neapolitan pizza with tomato, mozzarella, and basil.",
            imageUrl = "https://example.com/images/pizza.jpg",
            flagImageUrl = "https://flagcdn.com/w320/it.png",
            countLikes = 140,
            ingredients = "Pizza dough, tomato sauce, mozzarella, basil",
            country = "Italy"
        ),
        Dish(
            id = "it3",
            dishName = "Lasagna",
            dishDescription = "Layered pasta with meat sauce and cheese.",
            imageUrl = "https://example.com/images/lasagna.jpg",
            flagImageUrl = "https://flagcdn.com/w320/it.png",
            countLikes = 120,
            ingredients = "Lasagna noodles, meat sauce, ricotta, mozzarella",
            country = "Italy"
        ),
        Dish(
            id = "it4",
            dishName = "Risotto",
            dishDescription = "Creamy risotto with mushrooms.",
            imageUrl = "https://example.com/images/risotto.jpg",
            flagImageUrl = "https://flagcdn.com/w320/it.png",
            countLikes = 95,
            ingredients = "Arborio rice, mushrooms, Parmesan, white wine",
            country = "Italy"
        ),
        Dish(
            id = "it5",
            dishName = "Tiramisu",
            dishDescription = "Coffee-flavored dessert with mascarpone and ladyfingers.",
            imageUrl = "https://example.com/images/tiramisu.jpg",
            flagImageUrl = "https://flagcdn.com/w320/it.png",
            countLikes = 150,
            ingredients = "Ladyfingers, mascarpone, coffee, cocoa powder",
            country = "Italy"
        ),

        // Germany
        Dish(
            id = "de1",
            dishName = "Bratwurst",
            dishDescription = "Grilled sausage with mustard.",
            imageUrl = "https://example.com/images/bratwurst.jpg",
            flagImageUrl = "https://flagcdn.com/w320/de.png",
            countLikes = 80,
            ingredients = "Pork sausage, spices",
            country = "Germany"
        ),
        Dish(
            id = "de2",
            dishName = "Sauerkraut",
            dishDescription = "Fermented cabbage.",
            imageUrl = "https://example.com/images/sauerkraut.jpg",
            flagImageUrl = "https://flagcdn.com/w320/de.png",
            countLikes = 70,
            ingredients = "Cabbage, salt",
            country = "Germany"
        ),
        Dish(
            id = "de3",
            dishName = "Schnitzel",
            dishDescription = "Breaded and fried meat cutlet.",
            imageUrl = "https://example.com/images/schnitzel.jpg",
            flagImageUrl = "https://flagcdn.com/w320/de.png",
            countLikes = 90,
            ingredients = "Veal or pork, breadcrumbs, eggs",
            country = "Germany"
        ),
        Dish(
            id = "de4",
            dishName = "Pretzel",
            dishDescription = "Soft, baked pretzel.",
            imageUrl = "https://example.com/images/pretzel.jpg",
            flagImageUrl = "https://flagcdn.com/w320/de.png",
            countLikes = 60,
            ingredients = "Flour, water, salt, yeast",
            country = "Germany"
        ),
        Dish(
            id = "de5",
            dishName = "Currywurst",
            dishDescription = "Sausage served with curry ketchup.",
            imageUrl = "https://example.com/images/currywurst.jpg",
            flagImageUrl = "https://flagcdn.com/w320/de.png",
            countLikes = 85,
            ingredients = "Sausage, curry sauce",
            country = "Germany"
        ),

        // Spain
        Dish(
            id = "es1",
            dishName = "Paella",
            dishDescription = "Traditional rice dish with seafood.",
            imageUrl = "https://example.com/images/paella.jpg",
            flagImageUrl = "https://flagcdn.com/w320/es.png",
            countLikes = 110,
            ingredients = "Rice, seafood, saffron, vegetables",
            country = "Spain"
        ),
        Dish(
            id = "es2",
            dishName = "Tapas",
            dishDescription = "Assorted small dishes.",
            imageUrl = "https://example.com/images/tapas.jpg",
            flagImageUrl = "https://flagcdn.com/w320/es.png",
            countLikes = 75,
            ingredients = "Various small plates",
            country = "Spain"
        ),
        Dish(
            id = "es3",
            dishName = "Gazpacho",
            dishDescription = "Cold tomato-based soup.",
            imageUrl = "https://example.com/images/gazpacho.jpg",
            flagImageUrl = "https://flagcdn.com/w320/es.png",
            countLikes = 65,
            ingredients = "Tomatoes, cucumbers, peppers, garlic",
            country = "Spain"
        ),
        Dish(
            id = "es4",
            dishName = "Tortilla Española",
            dishDescription = "Spanish omelette with potatoes and onions.",
            imageUrl = "https://example.com/images/tortilla.jpg",
            flagImageUrl = "https://flagcdn.com/w320/es.png",
            countLikes = 80,
            ingredients = "Eggs, potatoes, onions",
            country = "Spain"
        ),
        Dish(
            id = "es5",
            dishName = "Churros",
            dishDescription = "Fried dough pastry often served with chocolate.",
            imageUrl = "https://example.com/images/churros.jpg",
            flagImageUrl = "https://flagcdn.com/w320/es.png",
            countLikes = 70,
            ingredients = "Flour, water, sugar, oil",
            country = "Spain"
        ),

        // Japan
        Dish(
            id = "jp1",
            dishName = "Sushi",
            dishDescription = "Vinegared rice with seafood.",
            imageUrl = "https://example.com/images/sushi.jpg",
            flagImageUrl = "https://flagcdn.com/w320/jp.png",
            countLikes = 140,
            ingredients = "Rice, raw fish, seaweed",
            country = "Japan"
        ),
        Dish(
            id = "jp2",
            dishName = "Ramen",
            dishDescription = "Noodle soup with flavorful broth.",
            imageUrl = "https://example.com/images/ramen.jpg",
            flagImageUrl = "https://flagcdn.com/w320/jp.png",
            countLikes = 130,
            ingredients = "Noodles, broth, pork, egg, scallions",
            country = "Japan"
        ),
        Dish(
            id = "jp3",
            dishName = "Tempura",
            dishDescription = "Lightly battered and fried seafood and vegetables.",
            imageUrl = "https://example.com/images/tempura.jpg",
            flagImageUrl = "https://flagcdn.com/w320/jp.png",
            countLikes = 120,
            ingredients = "Seafood, vegetables, batter",
            country = "Japan"
        ),
        Dish(
            id = "jp4",
            dishName = "Tonkatsu",
            dishDescription = "Breaded, deep-fried pork cutlet.",
            imageUrl = "https://example.com/images/tonkatsu.jpg",
            flagImageUrl = "https://flagcdn.com/w320/jp.png",
            countLikes = 110,
            ingredients = "Pork, breadcrumbs, cabbage",
            country = "Japan"
        ),
        Dish(
            id = "jp5",
            dishName = "Soba",
            dishDescription = "Buckwheat noodles served hot or cold.",
            imageUrl = "https://example.com/images/soba.jpg",
            flagImageUrl = "https://flagcdn.com/w320/jp.png",
            countLikes = 100,
            ingredients = "Buckwheat, wheat flour",
            country = "Japan"
        ),

        // China
        Dish(
            id = "cn1",
            dishName = "Kung Pao Chicken",
            dishDescription = "Spicy stir-fried chicken with peanuts.",
            imageUrl = "https://example.com/images/kungpao.jpg",
            flagImageUrl = "https://flagcdn.com/w320/cn.png",
            countLikes = 130,
            ingredients = "Chicken, peanuts, chili, Sichuan pepper",
            country = "China"
        ),
        Dish(
            id = "cn2",
            dishName = "Dim Sum",
            dishDescription = "Assorted bite-sized dumplings and dishes.",
            imageUrl = "https://example.com/images/dimsum.jpg",
            flagImageUrl = "https://flagcdn.com/w320/cn.png",
            countLikes = 120,
            ingredients = "Dumplings, buns, rolls",
            country = "China"
        ),
        Dish(
            id = "cn3",
            dishName = "Peking Duck",
            dishDescription = "Crispy roasted duck with pancakes and hoisin sauce.",
            imageUrl = "https://example.com/images/pekingduck.jpg",
            flagImageUrl = "https://flagcdn.com/w320/cn.png",
            countLikes = 110,
            ingredients = "Duck, pancakes, hoisin sauce",
            country = "China"
        ),
        Dish(
            id = "cn4",
            dishName = "Hot Pot",
            dishDescription = "A simmering pot of broth with various ingredients.",
            imageUrl = "https://example.com/images/hotpot.jpg",
            flagImageUrl = "https://flagcdn.com/w320/cn.png",
            countLikes = 100,
            ingredients = "Broth, meat, vegetables, tofu",
            country = "China"
        ),
        Dish(
            id = "cn5",
            dishName = "Mapo Tofu",
            dishDescription = "Spicy tofu in a flavorful sauce.",
            imageUrl = "https://example.com/images/mapotofu.jpg",
            flagImageUrl = "https://flagcdn.com/w320/cn.png",
            countLikes = 90,
            ingredients = "Tofu, chili bean paste, minced meat",
            country = "China"
        ),

        // India
        Dish(
            id = "in1",
            dishName = "Butter Chicken",
            dishDescription = "Creamy tomato-based chicken curry.",
            imageUrl = "https://example.com/images/butterchicken.jpg",
            flagImageUrl = "https://flagcdn.com/w320/in.png",
            countLikes = 150,
            ingredients = "Chicken, tomato, cream, spices",
            country = "India"
        ),
        Dish(
            id = "in2",
            dishName = "Biryani",
            dishDescription = "Spiced rice with meat and vegetables.",
            imageUrl = "https://example.com/images/biryani.jpg",
            flagImageUrl = "https://flagcdn.com/w320/in.png",
            countLikes = 140,
            ingredients = "Rice, meat, spices, vegetables",
            country = "India"
        ),
        Dish(
            id = "in3",
            dishName = "Samosa",
            dishDescription = "Fried pastry filled with spicy potatoes and peas.",
            imageUrl = "https://example.com/images/samosa.jpg",
            flagImageUrl = "https://flagcdn.com/w320/in.png",
            countLikes = 120,
            ingredients = "Potatoes, peas, spices, pastry",
            country = "India"
        ),
        Dish(
            id = "in4",
            dishName = "Tandoori Chicken",
            dishDescription = "Chicken marinated in yogurt and spices, then grilled.",
            imageUrl = "https://example.com/images/tandoorichicken.jpg",
            flagImageUrl = "https://flagcdn.com/w320/in.png",
            countLikes = 130,
            ingredients = "Chicken, yogurt, spices",
            country = "India"
        ),
        Dish(
            id = "in5",
            dishName = "Chole Bhature",
            dishDescription = "Spicy chickpea curry served with fried bread.",
            imageUrl = "https://example.com/images/cholebhature.jpg",
            flagImageUrl = "https://flagcdn.com/w320/in.png",
            countLikes = 110,
            ingredients = "Chickpeas, spices, flour",
            country = "India"
        ),

        // Brazil
        Dish(
            id = "br1",
            dishName = "Feijoada",
            dishDescription = "Hearty black bean stew with pork and beef.",
            imageUrl = "https://example.com/images/feijoada.jpg",
            flagImageUrl = "https://flagcdn.com/w320/br.png",
            countLikes = 140,
            ingredients = "Black beans, pork, beef, spices",
            country = "Brazil"
        ),
        Dish(
            id = "br2",
            dishName = "Pão de Queijo",
            dishDescription = "Cheese bread made with tapioca flour.",
            imageUrl = "https://example.com/images/paodequeijo.jpg",
            flagImageUrl = "https://flagcdn.com/w320/br.png",
            countLikes = 130,
            ingredients = "Tapioca flour, cheese, eggs",
            country = "Brazil"
        ),
        Dish(
            id = "br3",
            dishName = "Churrasco",
            dishDescription = "Brazilian barbecue with assorted meats.",
            imageUrl = "https://example.com/images/churrasco.jpg",
            flagImageUrl = "https://flagcdn.com/w320/br.png",
            countLikes = 150,
            ingredients = "Meats, spices, barbecue sauce",
            country = "Brazil"
        ),
        Dish(
            id = "br4",
            dishName = "Moqueca",
            dishDescription = "Fish stew with coconut milk and palm oil.",
            imageUrl = "https://example.com/images/moqueca.jpg",
            flagImageUrl = "https://flagcdn.com/w320/br.png",
            countLikes = 120,
            ingredients = "Fish, coconut milk, palm oil, spices",
            country = "Brazil"
        ),
        Dish(
            id = "br5",
            dishName = "Coxinha",
            dishDescription = "Chicken croquette shaped like a teardrop.",
            imageUrl = "https://example.com/images/coxinha.jpg",
            flagImageUrl = "https://flagcdn.com/w320/br.png",
            countLikes = 110,
            ingredients = "Chicken, dough, spices",
            country = "Brazil"
        ),

        // Mexico
        Dish(
            id = "mx1",
            dishName = "Tacos",
            dishDescription = "Corn tortillas filled with meat and toppings.",
            imageUrl = "https://example.com/images/tacos.jpg",
            flagImageUrl = "https://flagcdn.com/w320/mx.png",
            countLikes = 130,
            ingredients = "Corn tortillas, meat, salsa",
            country = "Mexico"
        ),
        Dish(
            id = "mx2",
            dishName = "Enchiladas",
            dishDescription = "Tortillas rolled around a filling and covered with sauce.",
            imageUrl = "https://example.com/images/enchiladas.jpg",
            flagImageUrl = "https://flagcdn.com/w320/mx.png",
            countLikes = 120,
            ingredients = "Tortillas, chicken, cheese, sauce",
            country = "Mexico"
        ),
        Dish(
            id = "mx3",
            dishName = "Guacamole",
            dishDescription = "Avocado-based dip.",
            imageUrl = "https://example.com/images/guacamole.jpg",
            flagImageUrl = "https://flagcdn.com/w320/mx.png",
            countLikes = 100,
            ingredients = "Avocado, tomato, onion, lime",
            country = "Mexico"
        ),
        Dish(
            id = "mx4",
            dishName = "Chiles en Nogada",
            dishDescription = "Stuffed peppers in walnut sauce.",
            imageUrl = "https://example.com/images/chilesennogada.jpg",
            flagImageUrl = "https://flagcdn.com/w320/mx.png",
            countLikes = 110,
            ingredients = "Peppers, meat, walnuts, pomegranate",
            country = "Mexico"
        ),
        Dish(
            id = "mx5",
            dishName = "Quesadilla",
            dishDescription = "Tortilla filled with cheese, sometimes with added ingredients.",
            imageUrl = "https://example.com/images/quesadilla.jpg",
            flagImageUrl = "https://flagcdn.com/w320/mx.png",
            countLikes = 90,
            ingredients = "Tortilla, cheese, optional fillings",
            country = "Mexico"
        ),

        // Canada
        Dish(
            id = "ca1",
            dishName = "Poutine",
            dishDescription = "Fries topped with cheese curds and gravy.",
            imageUrl = "https://example.com/images/poutine.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ca.png",
            countLikes = 120,
            ingredients = "Fries, cheese curds, gravy",
            country = "Canada"
        ),
        Dish(
            id = "ca2",
            dishName = "Butter Tarts",
            dishDescription = "Sweet tart pastry with a buttery filling.",
            imageUrl = "https://example.com/images/buttertarts.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ca.png",
            countLikes = 100,
            ingredients = "Pastry, butter, sugar, eggs",
            country = "Canada"
        ),
        Dish(
            id = "ca3",
            dishName = "Nanaimo Bars",
            dishDescription = "No-bake layered dessert.",
            imageUrl = "https://example.com/images/nanaimobars.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ca.png",
            countLikes = 90,
            ingredients = "Crumb base, custard, chocolate topping",
            country = "Canada"
        ),
        Dish(
            id = "ca4",
            dishName = "Tourtière",
            dishDescription = "Traditional meat pie.",
            imageUrl = "https://example.com/images/tourtiere.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ca.png",
            countLikes = 80,
            ingredients = "Ground meat, spices, pastry",
            country = "Canada"
        ),
        Dish(
            id = "ca5",
            dishName = "BeaverTails",
            dishDescription = "Fried dough pastry, often topped with sugar.",
            imageUrl = "https://example.com/images/beavertails.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ca.png",
            countLikes = 110,
            ingredients = "Dough, sugar, cinnamon",
            country = "Canada"
        ),

        // Australia
        Dish(
            id = "au1",
            dishName = "Meat Pie",
            dishDescription = "Savory pie filled with minced meat.",
            imageUrl = "https://example.com/images/meatpie.jpg",
            flagImageUrl = "https://flagcdn.com/w320/au.png",
            countLikes = 100,
            ingredients = "Minced meat, pastry, gravy",
            country = "Australia"
        ),
        Dish(
            id = "au2",
            dishName = "Vegemite Toast",
            dishDescription = "Toast spread with Vegemite.",
            imageUrl = "https://example.com/images/vegemitetoast.jpg",
            flagImageUrl = "https://flagcdn.com/w320/au.png",
            countLikes = 90,
            ingredients = "Bread, Vegemite",
            country = "Australia"
        ),
        Dish(
            id = "au3",
            dishName = "Pavlova",
            dishDescription = "Meringue dessert topped with fresh fruit and cream.",
            imageUrl = "https://example.com/images/pavlova.jpg",
            flagImageUrl = "https://flagcdn.com/w320/au.png",
            countLikes = 95,
            ingredients = "Meringue, fruit, whipped cream",
            country = "Australia"
        ),
        Dish(
            id = "au4",
            dishName = "Lamingtons",
            dishDescription = "Sponge cake coated in chocolate and coconut.",
            imageUrl = "https://example.com/images/lamingtons.jpg",
            flagImageUrl = "https://flagcdn.com/w320/au.png",
            countLikes = 85,
            ingredients = "Sponge cake, chocolate, coconut",
            country = "Australia"
        ),
        Dish(
            id = "au5",
            dishName = "Barramundi",
            dishDescription = "Grilled barramundi fish with spices.",
            imageUrl = "https://example.com/images/barramundi.jpg",
            flagImageUrl = "https://flagcdn.com/w320/au.png",
            countLikes = 80,
            ingredients = "Barramundi, spices, lemon",
            country = "Australia"
        ),

        // Russia
        Dish(
            id = "ru1",
            dishName = "Borscht",
            dishDescription = "Beet soup with cabbage and beef.",
            imageUrl = "https://example.com/images/borscht.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ru.png",
            countLikes = 110,
            ingredients = "Beets, cabbage, beef, potatoes",
            country = "Russia"
        ),
        Dish(
            id = "ru2",
            dishName = "Beef Stroganoff",
            dishDescription = "Sautéed beef in a sour cream sauce.",
            imageUrl = "https://example.com/images/stroganoff.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ru.png",
            countLikes = 120,
            ingredients = "Beef, sour cream, mushrooms, onions",
            country = "Russia"
        ),
        Dish(
            id = "ru3",
            dishName = "Pelmeni",
            dishDescription = "Meat-filled dumplings.",
            imageUrl = "https://example.com/images/pelmeni.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ru.png",
            countLikes = 90,
            ingredients = "Meat, flour, spices",
            country = "Russia"
        ),
        Dish(
            id = "ru4",
            dishName = "Blini",
            dishDescription = "Thin pancakes often served with caviar.",
            imageUrl = "https://example.com/images/blini.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ru.png",
            countLikes = 80,
            ingredients = "Flour, milk, eggs",
            country = "Russia"
        ),
        Dish(
            id = "ru5",
            dishName = "Pirozhki",
            dishDescription = "Small stuffed buns.",
            imageUrl = "https://example.com/images/pirozhki.jpg",
            flagImageUrl = "https://flagcdn.com/w320/ru.png",
            countLikes = 85,
            ingredients = "Flour, meat, vegetables",
            country = "Russia"
        ),

        // South Korea
        Dish(
            id = "kr1",
            dishName = "Kimchi",
            dishDescription = "Fermented cabbage and radish, a staple in Korean cuisine.",
            imageUrl = "https://example.com/images/kimchi.jpg",
            flagImageUrl = "https://flagcdn.com/w320/kr.png",
            countLikes = 130,
            ingredients = "Cabbage, radish, chili pepper, garlic",
            country = "South Korea"
        ),
        Dish(
            id = "kr2",
            dishName = "Bibimbap",
            dishDescription = "Mixed rice with vegetables and meat topped with a fried egg.",
            imageUrl = "https://example.com/images/bibimbap.jpg",
            flagImageUrl = "https://flagcdn.com/w320/kr.png",
            countLikes = 120,
            ingredients = "Rice, vegetables, meat, egg, chili paste",
            country = "South Korea"
        ),
        Dish(
            id = "kr3",
            dishName = "Bulgogi",
            dishDescription = "Marinated, grilled beef slices.",
            imageUrl = "https://example.com/images/bulgogi.jpg",
            flagImageUrl = "https://flagcdn.com/w320/kr.png",
            countLikes = 110,
            ingredients = "Beef, soy sauce, garlic, sugar, sesame oil",
            country = "South Korea"
        ),
        Dish(
            id = "kr4",
            dishName = "Tteokbokki",
            dishDescription = "Spicy rice cakes in a chili sauce.",
            imageUrl = "https://example.com/images/tteokbokki.jpg",
            flagImageUrl = "https://flagcdn.com/w320/kr.png",
            countLikes = 100,
            ingredients = "Rice cakes, chili paste, vegetables",
            country = "South Korea"
        ),
        Dish(
            id = "kr5",
            dishName = "Samgyeopsal",
            dishDescription = "Grilled pork belly served with lettuce wraps.",
            imageUrl = "https://example.com/images/samgyeopsal.jpg",
            flagImageUrl = "https://flagcdn.com/w320/kr.png",
            countLikes = 90,
            ingredients = "Pork belly, lettuce, garlic, dipping sauce",
            country = "South Korea"
        )
    )

    // Function to get dishes for a given country.
    fun getDishesByCountry(country: String): List<Dish> {
        return dishes.filter { it.country.equals(country, ignoreCase = true) }
    }

    // Function to get all dishes.
    fun getAllDishes(): List<Dish> {
        return dishes
    }
}
