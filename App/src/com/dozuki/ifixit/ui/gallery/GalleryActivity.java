package com.dozuki.ifixit.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dozuki.ifixit.MainApplication;
import com.dozuki.ifixit.R;
import com.dozuki.ifixit.model.user.LoginEvent;
import com.dozuki.ifixit.ui.BaseActivity;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.HashMap;

public class GalleryActivity extends BaseActivity {

   public static final String MEDIA_FRAGMENT_PHOTOS = "MEDIA_FRAGMENT_PHOTOS";
   public static final String MEDIA_FRAGMENT_VIDEOS = "MEDIA_FRAGMENT_VIDEOS";
   public static final String MEDIA_FRAGMENT_EMBEDS = "MEDIA_FRAGMENT_EMBEDS";
   // for return values
   public static final String ACTIVITY_RETURN_MODE = "ACTIVITY_RETURN_ID";

   private static final String LOGIN_VISIBLE = "LOGIN_VISIBLE";
   private static final String LOGIN_FRAGMENT = "LOGIN_FRAGMENT";

   private static final String SHOWING_HELP = "SHOWING_HELP";
   private static final String SHOWING_LOGOUT = "SHOWING_LOGOUT";
   private static final String SHOWING_DELETE = "SHOWING_DELETE";
   public static final String MEDIA_RETURN_KEY = "MEDIA_RETURN_KEY";
   public static final String FILTER_LIST_KEY = "FILTER_LIST_KEY";

   public static boolean showingLogout;
   public static boolean showingHelp;
   public static boolean showingDelete;

   private HashMap<String, MediaFragment> mMediaCategoryFragments;
   private MediaFragment mCurrentMediaFragment;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(((MainApplication) getApplication()).getSiteTheme());

      mMediaCategoryFragments = new HashMap<String, MediaFragment>();
      mMediaCategoryFragments.put(MEDIA_FRAGMENT_PHOTOS, new PhotoMediaFragment());

      /*
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_VIDEOS,
       * new VideoMediaFragment());
       * mMediaCategoryFragments.put(MEDIA_FRAGMENT_EMBEDS,
       * new EmbedMediaFragment());
       */
      mCurrentMediaFragment = mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);

      showingHelp = false;
      showingLogout = false;
      showingDelete = false;

      boolean getMediaItemForReturn = false;
      int mReturnValue = -1;

      if (getIntent().getExtras() != null) {
         Bundle bundle = getIntent().getExtras();
         mReturnValue = bundle.getInt(ACTIVITY_RETURN_MODE, -1);
         if (mReturnValue != -1) {
            getMediaItemForReturn = true;
         }
         startActionMode(new ContextualMediaSelect(this));
//         mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS).setForReturn(mMediaReturnValue);
//         mMediaCategoryFragments.get(MEDIA_FRAGMENT_VIDEOS).setForReturn(mMediaReturnValue);
//         mMediaCategoryFragments.get(MEDIA_FRAGMENT_EMBEDS).setForReturn(mMediaReturnValue);
      }

      mCurrentMediaFragment.setForReturn(getMediaItemForReturn);

      super.onCreate(savedInstanceState);

      setContentView(R.layout.gallery_root);
      StepAdapter stepAdapter = new StepAdapter(this.getSupportFragmentManager());
      ViewPager pager = (ViewPager) findViewById(R.id.gallery_view_body_pager);
      pager.setAdapter(stepAdapter);
      TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.gallery_view_top_bar);
      titleIndicator.setViewPager(pager);
      pager.setCurrentItem(1);

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(SHOWING_DELETE, showingDelete);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      boolean isLoggedIn = ((MainApplication) getApplication()).isUserLoggedIn();
      switch (item.getItemId()) {
         case R.id.top_camera_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchCamera();
            return true;
         case R.id.top_gallery_button:
            if (!isLoggedIn) {
               return false;
            }
            mCurrentMediaFragment.launchImageChooser();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Subscribe
   public void onLogin(LoginEvent.Login event) {

      if (MainApplication.get().isFirstTimeGalleryUser()) {
         createHelpDialog().show();
         MainApplication.get().setFirstTimeGalleryUser(false);
      }
   }

   @Override
   public boolean finishActivityIfLoggedOut() {
      return true;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getSupportMenuInflater().inflate(R.menu.gallery_menu, menu);

      return super.onCreateOptionsMenu(menu);
   }

   public class StepAdapter extends FragmentStatePagerAdapter {

      public StepAdapter(FragmentManager fm) {
         super(fm);
      }

      @Override
      public int getCount() {
         return mMediaCategoryFragments.size();
      }

      @Override
      public CharSequence getPageTitle(int position) {
         return "Images";
         /*
          * switch (position) {
          * case 0:
          * return "Videos";
          * case 1:
          * return "Photos";
          * case 2:
          * return "Embeds";
          * default:
          * return "Photos";
          * }
          */
      }

      @Override
      public Fragment getItem(int position) {
         return (PhotoMediaFragment) mMediaCategoryFragments.get(MEDIA_FRAGMENT_PHOTOS);
         /*
          * switch (position) {
          * case 0:
          * return (VideoMediaFragment) mMediaCategoryFragments
          * .get(MEDIA_FRAGMENT_VIDEOS);
          * case 1:
          * return (PhotoMediaFragment) mMediaCategoryFragments
          * .get(MEDIA_FRAGMENT_PHOTOS);
          * case 2:
          * return (EmbedMediaFragment) mMediaCategoryFragments
          * .get(MEDIA_FRAGMENT_EMBEDS);
          * default:
          * return (PhotoMediaFragment) mMediaCategoryFragments
          * .get(MEDIA_FRAGMENT_PHOTOS);
          * }
          */
      }

      @Override
      public void setPrimaryItem(ViewGroup container, int position, Object object) {
         super.setPrimaryItem(container, position, object);
         mCurrentMediaFragment = (MediaFragment) object;
      }
   }

   private AlertDialog createHelpDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.media_help_title)).setMessage(getString(R.string.media_help_messege,
       MainApplication.get().getSite().siteName()))
         .setPositiveButton(getString(R.string.media_help_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
            }
         });

      return builder.create();
   }

   public final class ContextualMediaSelect implements ActionMode.Callback {
      private Context mParentContext;

      public ContextualMediaSelect(Context parentContext) {
         mParentContext = parentContext;
      }

      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Create the menu from the xml file
         // MenuInflater inflater = getSupportMenuInflater();
         // inflater.inflate(R.menu.gallery_menu, menu);
         return true;
      }

      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false;
      }

      @Override
      public void onDestroyActionMode(ActionMode mode) {
         finish();
      }

      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

         return true;
      }
   }
}
