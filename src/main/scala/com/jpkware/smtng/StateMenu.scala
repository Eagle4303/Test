/*
 * This file is part of StarMines: The Next Generation - Copyright 2018-2019 Jari Karjala - www.jpkware.com
 * SPDX-License-Identifier: GPLv3-only
 */
package com.jpkware.smtng

import com.definitelyscala.phaser.{BitmapText, Game, Group, State}
import org.scalajs.dom.html

class StateMenu(game: Game, options: Map[String,String]) extends State {

  var infoTexts: Group = _
  var scoreTexts: Group = _
  var scoreText: BitmapText = _
  var scores: Seq[HighScore] = Seq()
  var scoresShown: Boolean = _
  val ScoreListLen = 10

  override def preload(): Unit = {
  }

  override def create(): Unit = {
    game.add.sprite(0,0, GlobalRes.MenuBg).scale.set(2,2)

    GlobalRes.drawLogo(game, 200)
    GlobalRes.drawCopy(game)

    StarMinesNG.shareButtonVisible(true)

    val help = if (game.device.desktop && !options.contains("touch"))
      "Control your ship with z,x,n,m, or arrow keys and space, or mouse"
    else
      "Use the touch buttons to control your ship, Pause menu to adjust the button layout"

    val infoY = game.height-700
    infoTexts = game.add.group(name="infotexts")
    infoTexts.add(game.add.bitmapText(game.width/2,infoY+100, GlobalRes.FontId, s"Welcome ${Progress.getName}!", 40))
    infoTexts.add(game.add.bitmapText(game.width/2,infoY+300, GlobalRes.FontId,
      "Collect all Bonusoids for maximum score and ship upgrades", 32))
    infoTexts.add(game.add.bitmapText(game.width/2,infoY+350, GlobalRes.FontId, help, 32))
    infoTexts.forEach((text: BitmapText) => { text.anchor.set(0.5,0.5) }, null, false)

    scoreTexts = game.add.group(name="scores")
    scoreText = game.add.bitmapText(game.width/2,infoY, GlobalRes.FontId, "", 32)
    scoreTexts.add(scoreText)
    scoreTexts.add(game.add.bitmapText(game.width/2,infoY-60, GlobalRes.FontId, "Game High Scores:", 40))
    scoreTexts.forEach((text: BitmapText) => { text.anchor.set(0.5,0) }, null, false)
    showTexts()

    val (b1x,b2x,b3x) = if (Progress.hasCheckpoint) (-200,0,200) else (-100, 0, 100)
    val buttonY = game.height-180

    val button = PhaserButton.add(game, game.width/2+b1x, buttonY, "", scale = 1.0, textFrame=PhaserButton.FramePlay)
    button.events.onInputUp.add(startGame _, null, 1)

    if (Progress.hasCheckpoint) PhaserButton.addRetry(game, game.width/2+b2x, buttonY)

    PhaserButton.addLevels(game, game.width/2+b3x, buttonY)

    PhaserButton.addMinMax(game)

    val infoButton = PhaserButton.add(game, game.width - 64, 64, "Info", scale = 0.6)
    infoButton.events.onInputUp.add(() => gotoInfo(), null, 1)

    fetchHighScores()
    val timer = game.time.create(true)
    timer.loop(10000, () => {
      if (scoresShown) showTexts() else showScores()
    }, null)
    timer.start(0)

    game.sound.volume = 0.5
    game.sound.muteOnPause = true
    if (GlobalRes.GameMusic.isPlaying) GlobalRes.GameMusic.stop()
    if (!GlobalRes.MenuMusic.isPlaying) GlobalRes.MenuMusic.loopFull(GlobalRes.MusicVolume)
  }

  override def update(): Unit = {
    if (Progress.hasCheckpoint && (PhaserKeys.isRetryDown(game) || PhaserKeys.isFireDown(game))) PhaserButton.gotoRetry(game)
    else if (PhaserKeys.isFireDown(game)) startGame()

    if (PhaserKeys.isLevelsDown(game)) PhaserButton.gotoLevels(game)
  }

  def showTexts(): Unit = {
    infoTexts.visible = true
    scoreTexts.visible = false
    scoresShown = false
  }

  def showScores(): Unit = {
    infoTexts.visible = false
    if (!scoresShown) {
      scoreText.text = Progress.formatScores(scores)
      scoreTexts.visible = true
      scoresShown = true
    }
  }

  def fetchHighScores(): Unit = {
    Progress.fetchScores(None, ScoreListLen, (scores: Seq[HighScore]) => {this.scores = scores})
  }

  def startGame(): Unit = {
    StarMinesNG.shareButtonVisible(false)
    game.state.start("name", args = "start", clearCache = false, clearWorld = true)
  }

  def gotoInfo(): Unit = {
    game.state.start("info", args = "menu", clearCache = false, clearWorld = true)
  }
}