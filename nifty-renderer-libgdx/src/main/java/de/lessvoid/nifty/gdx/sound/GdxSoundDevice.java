package de.lessvoid.nifty.gdx.sound;

import com.badlogic.gdx.assets.AssetManager;

import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import de.lessvoid.nifty.spi.sound.SoundHandle;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;

import javax.annotation.Nonnull;

/**
 * This is the sound device that uses libGDX to play the sounds.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public class GdxSoundDevice implements SoundDevice {
  /**
   * The asset manager used to load the sound and music files.
   */
  private final AssetManager assetManager;

  /**
   * Create a new sound device that uses libGDX.
   *
   * @param assetManager the asset manager
   */
  public GdxSoundDevice(final AssetManager assetManager) {
    this.assetManager = assetManager;
  }

  @Override
  public void setResourceLoader(@Nonnull final NiftyResourceLoader niftyResourceLoader) {
    // nothing to do
  }

  @Nonnull
  @Override
  public SoundHandle loadSound(@Nonnull final SoundSystem soundSystem, @Nonnull final String filename) {
    return new GdxSoundHandle(assetManager, soundSystem, filename);
  }

  @Override
  public SoundHandle loadMusic(@Nonnull final SoundSystem soundSystem, @Nonnull final String filename) {
    return new GdxMusicHandle(assetManager, soundSystem, filename);
  }

  @Override
  public void update(final int delta) {
    // nothing to do
  }
}
