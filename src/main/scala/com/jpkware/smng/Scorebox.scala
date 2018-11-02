package com.jpkware.smng

import com.definitelyscala.phaser.{BitmapText, Game, Sprite}
import com.definitelyscala.phaser.Physics.Arcade.Body

case class ScoreState(var score: Int, var lives: Int, var level: Int, var bonusesCollected: Int)

class Scorebox(game: Game, scores: ScoreState) extends Sprite(game, 0,0, Scorebox.ScoreboxId) {

  var scoreText: BitmapText = _
  var livesText: BitmapText = _
  var levelText: BitmapText = _

  game.add.existing(this)
  position.set(game.width/2,game.height/2)
  anchor.set(0.5,0.5)
  game.physics.enable(this)
  body match { case b: Body => b.immovable = true}
  inputEnabled = true
  events.onInputUp.add(() => {
    if (game.scale.isFullScreen) game.scale.stopFullScreen() else game.scale.startFullScreen()
  }, null, 1)

  game.add.bitmapText(game.width/2,game.height/2-80, GlobalRes.FontId, "StarMines", 96).anchor.set(0.5,0.5)
  game.add.bitmapText(game.width/2,game.height/2-48, GlobalRes.FontId, "THE NEXT GENERATION", 32).anchor.set(0.5,0.5)
  game.add.bitmapText(game.width/2-280,game.height/2+20, GlobalRes.FontId, "Score:", 48)
  scoreText = game.add.bitmapText(game.width/2-96,game.height/2+20, GlobalRes.FontId, "", 48)

  game.add.bitmapText(game.width/2-280,game.height/2+60, GlobalRes.FontId, "Ships:", 48)
  livesText = game.add.bitmapText(game.width/2-96,game.height/2+60, GlobalRes.FontId, "", 48)

  game.add.bitmapText(game.width/2+60,game.height/2+60, GlobalRes.FontId, "Field:", 48)
  levelText = game.add.bitmapText(game.width/2+200,game.height/2+60, GlobalRes.FontId, "", 48)
  addToScore(0)
  addToLives(0)
  addToLevel(0)

  def addToBonusesCollected(delta: Int): Unit = {
    this.scores.bonusesCollected += delta
  }

  def addToLives(delta: Int): Unit = {
    this.scores.lives += delta
    livesText.setText(f"${scores.lives}%d")
  }

  def addToLevel(delta: Int): Unit = {
    this.scores.level += delta
    levelText.setText(f"${scores.level}%d")
  }

  def addToScore(delta: Int): Unit = {
    scores.score += delta
    if (scores.score<0) scores.score = 0
    scoreText.setText(f"${scores.score}%08d")
  }
}

object Scorebox {
  def ScoreboxId = "scorebox"
  def InitialScore = ScoreState(score = 0, lives = 5, level = 1, bonusesCollected = 0)
  def preloadResources(game: Game): Unit = {
    game.load.image(ScoreboxId, "res/scorebox.png")
  }
}
