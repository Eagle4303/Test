package com.jpkware.smng

import com.definitelyscala.phaser.{Game, _}

import scala.scalajs.js


class StateBoot(game: Game, options: Map[String,String]) extends State {

  override def init(args: js.Any*): Unit = {
    game.stage.disableVisibilityChange = true
    game.time.advancedTiming = true
    game.scale.fullScreenScaleMode = ScaleManager.SHOW_ALL
    game.scale.scaleMode = ScaleManager.SHOW_ALL
  }

  override def preload(): Unit = {
    game.load.image("preloaderBar", "res/flame.png")
    game.load.image("ship-preload", "res/ship-preload.png")
    game.load.image(GlobalRes.MenuBg, "res/space0.jpg")
  }

  override def create(): Unit = {
    game.input.addPointer() // 3rd touch
    game.input.addPointer() // 4th touch
    game.canvas.oncontextmenu = (e) => { e.preventDefault() }
    game.state.start("preloader", args = js.Array[String](), clearCache = false, clearWorld = false)
  }
}