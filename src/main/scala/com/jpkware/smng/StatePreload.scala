package com.jpkware.smng

import com.definitelyscala.phaser.{Game, Sprite}

class StatePreload(game: Game, options: Map[String,String]) {

  val state: State = State(preload, create, update)

  var preloadBar: Sprite = _

  def preload(): Unit = {

    val ship = game.add.sprite(game.width / 2, game.height / 2, "ship-preload")
    ship.anchor.set(1, 0.5)
    preloadBar = game.add.sprite(game.width / 2 - 16, game.height / 2, "preloaderBar")
    preloadBar.angle = 180
    preloadBar.anchor.set(1, 0.5)
    game.load.setPreloadSprite(preloadBar)

    game.load.image("space", "res/space1.jpg")
    game.load.image("missile", "res/missile.png")
    game.load.image("flame", "res/flame.png")
    game.load.image("scorebox", "res/scorebox.png")
    game.load.image("explo", "res/explo.png")
    game.load.bitmapFont("font", "res/font.png", "res/font.fnt")
    game.load.spritesheet("button", "res/button.png", 128, 128)
    game.load.atlasJSONHash("sprites", s"res/mainv2.png", s"res/mainv2.json")
    game.load.audio("sfx:zap", "res/zap.wav")
    game.load.audio("sfx:explo", "res/explo.wav")
    game.load.audio("sfx:tinyexp", "res/tinyexp.wav")
  }

  def create(): Unit = {
    game.state.start("play", args = null, clearCache = false, clearWorld = false)
  }

  def update(): Unit = {
  }

}