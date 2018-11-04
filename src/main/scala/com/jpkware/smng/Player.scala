package com.jpkware.smng

import com.definitelyscala.phaser.Physics.Arcade.Body
import com.definitelyscala.phaser._

class Player(game: Game, x: Double, y: Double)
  extends PreRotatedSprite(game, x,y, GlobalRes.MainAtlasId, Player.ShipPrefix, 64) {
  game.physics.arcade.enable(this)
  physBody.drag.set(10,10)
  physBody.maxVelocity.set(750,750)
  physBody.collideWorldBounds = true
  physBody.bounce.set(1,1)

  val weapon: Weapon = game.add.weapon(10, GlobalRes.MainAtlasId, Player.MissileId)
  weapon.bulletKillType = Weapon.KILL_LIFESPAN
  weapon.bulletLifespan = 1000
  weapon.bulletCollideWorldBounds = true
  weapon.bulletRotateToVelocity = true
  weapon.bulletSpeed = 1000
  weapon.bulletInheritSpriteSpeed = true
  weapon.fireRate = 300
  weapon.autofire = false
  weapon.bullets.forEach(setBounce _, null, checkExists = false)
  weapon.trackSprite(this, 0,0, trackRotation = false)

  val FlameScalaMax: Double = fullWidth/80
  val flame: Sprite = game.add.sprite(x,y,Player.FlameId)
  flame.anchor = new Point(1.0, 0.5)
  flame.visible = false
  var flameScale = 0.25
  game.physics.arcade.enable(flame)

  val sfxZap: Sound = game.add.audio(Player.SfxZapId)
  sfxZap.allowMultiple = true

  var immortal: Boolean = true
  revive()

  def rotateLeft(): Unit = setRotationSpeed(scala.math.Pi*2)
  def rotateRight(): Unit = setRotationSpeed(-scala.math.Pi*2)
  def rotateStop(): Unit = setRotationSpeed(0)

  def thrust(): Unit = {
    if (!this.visible) return

    game.physics.arcade.accelerationFromRotation(indexRotation, 500, physBody.acceleration)
    flame.revive(1)
    flame.rotation = indexRotation
    flame.position = headPoint(-fullWidth/6)
    flame.scale.set(flameScale, flameScale)
    if (flameScale<FlameScalaMax) flameScale += 0.1
    flame.body match {
      case body: Body =>
        body.acceleration = physBody.acceleration
        body.velocity = physBody.velocity
    }
  }
  def brake(): Unit = {
    physBody.acceleration.set(0,0)
    flame.kill()
    flameScale = 0.25
  }
  def stop(): Unit = {
    brake()
    physBody.velocity.set(0,0)
  }

  def hide(): Unit = {
    stop()
    visible = false
  }
  def fire(): Bullet = {
    if (!this.visible) return null
    weapon.fireAngle = indexAngle
    val bullet = weapon.fire(headPoint(fullWidth/2))
    if (bullet!=null) sfxZap.play()
    bullet
  }

  def setBounce(bullet: Bullet): Unit = {
    bullet.body match { case body: Body => body.bounce.set(1,1) }
  }

  override def kill(): Sprite = {
    stop()
    flame.kill()
    super.kill()
  }

  override def revive(health: Double = 1): Sprite = {
    alpha = 0.5
    flame.alpha = 0.5
    immortal = true
    val timer = game.time.create(true)
    timer.add(1000, () => {
      immortal = false
      alpha = 1.0
      flame.alpha = 1.0
    }, null)
    timer.start(0)

    super.revive(health)
  }
}

object Player {
  def MissileId = "missile"
  def FlameId = "flame"
  def SfxZapId = "sfx:zap"
  def ShipPrefix = "ship"

  def preloadResources(game: Game): Unit = {
    game.load.image(FlameId, "res/flame.png")
    game.load.audio(SfxZapId, "res/zap.wav")
  }
}