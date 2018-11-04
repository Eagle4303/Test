package com.jpkware.smng

import com.definitelyscala.phaser.Physics.Arcade.Body
import com.definitelyscala.phaser.{Bullet, Game, Group, Point}

class Splitter(game: Game, rule: Rule, group: Group) extends Enemy(game, rule, group) {
  val pieces: Group = game.add.group()
  val shape: String = rule.args(0)
  val frames: Int = rule.args(1).toInt
  val fps: Int = rule.args(2).toInt
  val count: Int = rule.args(3).toInt
  val score: Int = rule.score/count
  val pieceRule: Rule = Rule(null, shape, frames, fps, score, spd = rule.spd)

  (1 to count).foreach(i => {
    val b = new SplitterPiece(game, pieceRule, group)
    b.kill()
    pieces.add(b)
  })

  override def bulletHit(bullet: Bullet): Int = {
    super.bulletHit(bullet)
    (1 to count).foreach(i => {
      val b = pieces.getFirstDead()
      b match {
        case b: SplitterPiece =>
          b.reset(position.x, position.y, 1)
          group.add(b)
          b.body match {
            case body: Body =>
              val v = 200
              val vel = new Point(v*StarMinesNG.rnd.nextGaussian(),v*StarMinesNG.rnd.nextGaussian())
              body.velocity = vel
          }
        case _ => Logger.warn("No dead Pieces??")
      }
    })
    rule.score
  }
}

class SplitterPiece(game: Game, rule: Rule, group: Group) extends Enemy(game, rule, group) {
  // Nothing to do here
}

object Splitter {
  def spawn(game: Game, rule: Rule, group: Group) : Enemy = new Splitter(game, rule, group)
}