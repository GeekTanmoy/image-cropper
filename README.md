# Image Cropper Library

This is ***Image Cropper Library*** which provide functionalities like to Capture, Crop & Preview Image.

## Functionalities
  - Capture Image using Default Camera App.
  - Free Flow Cropping of Captured Image.
  - Preview the Cropped Image.
  - Return Image Uri to the source.

## Module Integration
**1. Add it in your root settings.gradle at the end of repositories:**

    dependencyResolutionManagement {
		  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		  repositories {
			  mavenCentral()
        
			  maven { url 'https://jitpack.io' } //Add this line
		  }
	  }

  **2. Add the dependency:**

    dependencies {
	    implementation 'com.github.GeekTanmoy:image-cropper:Tag'
	}

  For ***Tag***, please check the Latest Release.

  ## Implementation
  In your Activity or Fragment call the SDK.

  ### In your Activity

  	Intent(this, CropActivity::class.java).also { intent ->
        intent.putExtra("ACTION", "C") //For Camera
		intent.putExtra("ACTION", "C") //For Gallery
		fileLauncher.launch(intent)
    }

 ### In your Fragment

 	Intent(requireContext(), CropActivity::class.java).also { intent ->
        intent.putExtra("ACTION", "C") //For Camera
		intent.putExtra("ACTION", "C") //For Gallery
		fileLauncher.launch(intent)
    }

### Result 

	private val fileLauncher =
    	registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri")
            	//Use this imageUri as required
            }
    }


***Happy Coding***
