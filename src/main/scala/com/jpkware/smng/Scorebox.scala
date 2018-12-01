package com.jpkware.smng

import com.definitelyscala.phaser.{BitmapText, Game, Sprite}
import com.definitelyscala.phaser.Physics.Arcade.Body

case class ScoreState(var score: Int, var lives: Int, var level: Int,
                      var bonusoidsCollected: Int, var totalBonusoids: Int,
                      var timeBonus: Int, var stars: Int)

class Scorebox(game: Game, scores: ScoreState) extends Sprite(game, 0,0, Scorebox.ScoreboxId) {

  var scoreText: BitmapText = _
  var livesText: BitmapText = _
  var levelText: BitmapText = _
  var bonusText: BitmapText = _

  game.add.existing(this)
  position.set(game.width/2,game.height/2)
  anchor.set(0.5,0.5)
  game.physics.enable(this)
  body match { case b: Body => b.immovable = true}

  game.add.bitmapText(game.width/2,game.height/2-80, GlobalRes.FontId, "StarMines", 96).anchor.set(0.5,0.5)
  game.add.bitmapText(game.width/2,game.height/2-48, GlobalRes.FontId, "THE NEXT GENERATION", 28).anchor.set(0.5,0.5)

  game.add.bitmapText(game.width/2-280,game.height/2, GlobalRes.FontId, "Score:", 48)
  scoreText = game.add.bitmapText(game.width/2+280,game.height/2, GlobalRes.FontId, "", 48)
  scoreText.anchor.set(1,0)

  game.add.bitmapText(game.width/2-280,game.height/2+45, GlobalRes.FontId, "Bonus:", 48)
  bonusText = game.add.bitmapText(game.width/2+280,game.height/2+45, GlobalRes.FontId, "", 48)
  bonusText.anchor.set(1,0)

  livesText = game.add.bitmapText(game.width/2-280,game.height/2+90, GlobalRes.FontId, "", 48)
  livesText.anchor.set(0,0)

  levelText = game.add.bitmapText(game.width/2+280,game.height/2+90, GlobalRes.FontId, "", 48)
  levelText.anchor.set(1,0)

  addToScore(0)
  addToLives(0)
  addToLevel(0)
  addToTimeBonus(0)

  private val timer = game.time.create(true)
  timer.loop(500, () => {
    addToTimeBonus(-500)
  }, null)
  timer.start(0)

  def addToBonusoidsCollected(delta: Int): Unit = {
    this.scores.bonusoidsCollected += delta
    this.scores.totalBonusoids += delta
  }

  def addToLives(delta: Int): Unit = {
    this.scores.lives += delta
    livesText.setText(f"Ships: ${scores.lives}%d")
  }

  def addToLevel(delta: Int): Unit = {
    this.scores.level += delta
    levelText.setText(f"Field: ${scores.level}%d")
  }

  def addToScore(delta: Int): Unit = {
    scores.score += delta
    if (scores.score<0) scores.score = 0
    scoreText.setText(f"${scores.score}%d")
  }

  def addToTimeBonus(delta: Int): Unit = {
    if (scores.timeBonus + delta < 0) {
      scores.timeBonus = 0
      bonusText.setText(f"${scores.timeBonus}%d")
    }
    else {
      scores.timeBonus += delta
      bonusText.setText(f"${scores.timeBonus}%d")
    }
  }
}

object Scorebox {
  def ScoreboxId = "scorebox"
  def InitialScore = ScoreState(score = 0, lives = 5, level = 1, bonusoidsCollected = 0, totalBonusoids = 0, timeBonus = 0, stars = 0)
  def preloadResources(game: Game): Unit = {
    game.load.image(ScoreboxId, "res/scorebox.png")
  }
}
