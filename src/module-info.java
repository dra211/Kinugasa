/*
 * The MIT License
 *
 * Copyright 2023 Shinacho.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

module kinugasa {
	requires static com.h2database;
	requires java.base;
	requires transitive java.desktop;
	requires transitive java.sql;
	requires transitive java.logging;
	exports kinugasa.game.event.fb;
	exports kinugasa.game.field4;
	exports kinugasa.game.input;
	exports kinugasa.game.system;
	exports kinugasa.game.ui;
	exports kinugasa.game;
	exports kinugasa.graphics;
	exports kinugasa.object;
	exports kinugasa.object.movemodel;
	exports kinugasa.resource.db;
	exports kinugasa.resource.sound;
	exports kinugasa.resource.text;
	exports kinugasa.resource;
	exports kinugasa.util;	
//	opens kinugasa.game.event.fb;
//	opens kinugasa.game.field4;
//	opens kinugasa.game.input;
//	opens kinugasa.game.system;
//	opens kinugasa.game.ui;
//	opens kinugasa.game;
//	opens kinugasa.graphics;
//	opens kinugasa.object;
//	opens kinugasa.object.movemodel;
//	opens kinugasa.resource.db;
//	opens kinugasa.resource.sound;
//	opens kinugasa.resource.text;
//	opens kinugasa.resource;
//	opens kinugasa.util;
}
