package com.jpkware.smng

import com.definitelyscala.phaser.Physics.Arcade.Body
import com.definitelyscala.phaser.{Game, _}
import org.scalajs.dom.raw.Element

import scala.annotation.tailrec
import scala.scalajs.js


class StatePlay(game: Game, options: Map[String,String], status: Element) extends State {
  var player: Player = _
  var scorebox: Scorebox = _
  var scores: Scores = _
  var mines: Group = _
  var bonusManager: BonusManager = _
  var cursors: CursorKeys = _
  var fpsText: BitmapText = _
  var touch: TouchControls = _
  var gameOver = false
  var sfxLevelEnd: Sound = _
  var sfxLevelClr: Sound = _
  var sfxCollect: Sound = _
  var messages: Messages = _

  def mineCount: Int = if (options.contains("mines")) options("mines").toInt else 9 + scores.level

  override def init(args: js.Any*): Unit = {
    args.headOption match {
      case str: Some[js.Any] =>
        Logger.info(s"init ${str.get}")
        if (str.get.asInstanceOf[String]=="nextlevel") {
          scorebox.updateLevel(1)
          StarMinesNG.rnd.setSeed(42+scores.level)
        }
        else {
          scores = Scorebox.InitialScore
        }
      case _ =>
    }
    gameOver = false
  }

  override def preload(): Unit = {
  }

  override def create(): Unit = {

    if (options.contains("debug")) {
      val button = PhaserButton.add(game, 128, 128, "")
      button.events.onInputUp.add(nextLevel _, null, 1)
    }

    val bg = if (scores.level-1 <= StarMinesNG.maxBackground) scores.level-1 else 6 + scores.level % (StarMinesNG.maxBackground-5)
    val space = game.add.sprite(0,0,s"space$bg")
    space.scale.set(2,2)

    touch = new TouchControls(game)
    if (options.contains("touch") || !game.device.desktop) touch.enable()

    game.physics.startSystem(PhysicsObj.ARCADE)

    player = new Player(game, 100,100)
    game.add.existing(player)

    cursors = game.input.keyboard.createCursorKeys()

    Explosion.initGroups(game, Seq(Explosion.LargeExploCount, Explosion.SmallExploCount))

    scorebox = new Scorebox(game, scores)

    fpsText = game.add.bitmapText(5,5, "font", "", 18)

    sfxLevelEnd = game.add.audio("sfx:levelend")
    sfxLevelClr = game.add.audio("sfx:levelclr")
    sfxCollect = game.add.audio("sfx:swip")

    bonusManager = new BonusManager(game, 2 + scala.math.min(scores.level/2, 8), findSafePosition)
    spawnMines()

    messages = new Messages(game)
  }

  override def update(): Unit = {
    if (gameOver) return
    handleCollisions()
    handleInput()
    if (options.contains("fps")) fpsText.setText(game.time.fps.toString)
  }

  override def render(): Unit = {
    // game.debug.bodyInfo(player, 32, 32)
    // game.debug.body(player)
    // game.debug.pointer(game.input.mousePointer)
  }

  def spawnMines(): Unit = {
    mines = game.add.group()

    (1 to mineCount).foreach(i => {
      val mine = new Mine(game, 0,0)
      findSafePosition(mine)
      mines.add(mine)
    })
  }

  @tailrec
  private def findSafePosition(sprite: Sprite): Unit = {
    val x = StarMinesNG.rnd.nextFloat()*game.world.width
    val y = StarMinesNG.rnd.nextFloat()*game.world.height

    val sbb: Rectangle = scorebox.getBounds().asInstanceOf[Rectangle]
    // XXX the bounds are in local space if the sprite is not yet in world, transform manually
    if (sbb.x<0) {
      sbb.x += scorebox.position.x
      sbb.y += scorebox.position.y
    }
    val pbb: Rectangle = new Rectangle(0,0,200,200) // safe zone around player

    val mbb: Rectangle = sprite.getBounds().asInstanceOf[Rectangle]
    // The new position is not yet in effect; assume x,y is center
    mbb.x = x - mbb.width/2
    mbb.y = y - mbb.height/2
    if (!Rectangle.intersects(sbb,mbb) && !Rectangle.intersects(pbb,mbb)) {
      sprite.reset(x, y)
    }
    else {
      Logger.info(s"$x, $y was unsafe, retrying")
      findSafePosition(sprite)
    }
  }

  def handleCollisions(): Unit = {
    game.physics.arcade.collide(player, scorebox)
    game.physics.arcade.overlap(player, mines, playerVsEnemy _, null, null)
    game.physics.arcade.overlap(player, bonusManager.bonuses, playerVsBonus _, null, null)
    game.physics.arcade.collide(player.weapon.bullets, scorebox)
    game.physics.arcade.overlap(player.weapon.bullets, mines, bulletVsEnemy _, null, null)
    game.physics.arcade.overlap(player.weapon.bullets, bonusManager.bonusoids, bulletVsBonusoid _, null, null)
    game.physics.arcade.overlap(player.weapon.bullets, bonusManager.bonuses, bulletVsBonus _, null, null)
    game.physics.arcade.collide(mines, scorebox)
    game.physics.arcade.collide(bonusManager.bonuses, scorebox)
    if (bonusManager.bonusoids.countLiving()==0 && bonusManager.bonuses.countLiving()==0) nextLevel()
  }

  def nextLevel(): Unit = {
    val result = if (bonusManager.bonusCount == scores.bonusesCollected) {
      sfxLevelEnd.play()
      "Mine field complete\nAll Bonusoids collected!"
    }
    else {
      sfxLevelClr.play()
      "Mine field complete\nLost some Bonusoids..."
    }
    messages.show(result)
    touch.disable()
    mines.destroy()
    messages.clear()
    player.hide()
    game.state.start("nextlevel", args = result, clearCache = false, clearWorld = false)
  }

  def bulletVsEnemy(bullet: Bullet, enemy: Sprite): Unit = {
    Explosion(game, Explosion.SmallExploCount).explode(enemy)
    enemy.kill()
    bullet.kill()
    scorebox.addToScore(123)
  }

  def bulletVsBonusoid(bullet: Bullet, bonusoid: Sprite): Unit = {
    messages.show("Bonusoids released, go catch them!")
    Explosion(game, Explosion.SmallExploCount).explode(bonusoid)
    bonusoid.kill()
    bullet.kill()
    scorebox.addToScore(1000)
  }

  def bulletVsBonus(bullet: Bullet, bonus: Sprite): Unit = {
    messages.show("Bonusoid lost!")
    Explosion(game, Explosion.SmallExploCount).explode(bonus)
    bonus.kill()
    bullet.kill()
    scorebox.addToScore(100)
  }

  def playerVsBonus(player: Player, bonus: Sprite): Unit = {
    messages.show("Bonusoid collected!")
    sfxCollect.play()
    bonus.kill()
    scorebox.updateBonusesCollected(1)
    scorebox.addToScore(10000)
  }

  def playerVsEnemy(player: Player, enemy: Sprite): Unit = {
    if (player.immortal) {
      Explosion(game, Explosion.SmallExploCount).explode(enemy)
      enemy.kill()
    }
    else {
      messages.show("SHIP LOST!")
      Explosion(game, Explosion.LargeExploCount).explode(player)
      enemy.kill()
      player.kill()
      val timer = game.time.create(true)
      timer.add(3000, () => {
        scorebox.updateLives(-1)
        if (scores.lives==0) handleGameOver() else player.revive()
      }, null)
      timer.start(0)
    }
  }

  def handleGameOver(): Unit = {
    gameOver = true
    touch.disable()
    mines.destroy()
    player.hide()
    messages.clear()
    game.state.start("gameover", args = "gameover", clearCache = false, clearWorld = false)
  }

  def handleInput(): Unit = {
    val k = game.input.keyboard

    if (cursors.left.isDown || k.isDown('Z') || touch.rotateLeft) player.rotateLeft()
    else if (cursors.right.isDown || k.isDown('X') || touch.rotateRight) player.rotateRight()
    else player.rotateStop()

    if (cursors.up.isDown || k.isDown('N') || touch.thrust) player.thrust() else player.brake()

    if (PhaserKeys.isFireDown(game) || touch.fire) player.fire()

    if (k.isDown(27)) game.state.start("menu", args = "quit", clearCache = false, clearWorld = true)
  }
}