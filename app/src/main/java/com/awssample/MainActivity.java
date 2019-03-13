package com.awssample;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etSlogan,etCompanyName,etAddress,etCompanyMail,
            etPhone,etCompanyWebsite,etPersonName,etDesignation;

    private final String KEY = "xxx";
    private final String SECRET = "xxxxxx";
    private AmazonS3Client s3Client;
    private BasicAWSCredentials credentials;
    //track Choosing Image Intent
    private static final int CHOOSING_IMAGE_REQUEST = 1234;
  //  private TextView tvFileName;
    private ImageView imageView;
  //  private EditText edtFileName;

    private Uri fileUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);

        imageView = findViewById(R.id.img_file);
        //edtFileName = findViewById(R.id.edt_file_name);
       // tvFileName = findViewById(R.id.tv_file_name);

        findViewById(R.id.btn_choose_file).setOnClickListener(this);
        findViewById(R.id.btn_upload).setOnClickListener(this);

        initView();



        // findViewById(R.id.btn_download).setOnClickListener(this);

      //  AWSMobileClient.getInstance().initialize(this).execute();

      //  credentials = new BasicAWSCredentials(KEY, SECRET);
       // s3Client = new AmazonS3Client(credentials);
    }

    private void initView() {
        etCompanyName=findViewById(R.id.etCompanyName);
        etSlogan=findViewById(R.id.etSlogan);
        etAddress=findViewById(R.id.etAddress);
        etCompanyMail=findViewById(R.id.etCompanyMail);
        etPhone=findViewById(R.id.etPhone);
        etCompanyWebsite=findViewById(R.id.etCompanyWebsite);
        etPersonName=findViewById(R.id.etPersonName);
        etDesignation=findViewById(R.id.etDesignation);


    }
    private boolean validateFields(){
        if (etCompanyName.getText().toString().trim().isEmpty()){
            etCompanyName.setError("Please Enter Company Name");
            return false;
        }else if (etCompanyName.getText().toString().trim().length()<3){
            etCompanyName.setError("Please Enter Valid Company Name");
            return false;
        }else if (etAddress.getText().toString().trim().isEmpty()){
            etAddress.setError("Please Enter Valid Company Address");
            return false;
        }else if (etAddress.getText().toString().trim().length()<20){
            etAddress.setError("Please Enter Valid Company Address");
            return false;
        }else if (etCompanyMail.getText().toString().trim().isEmpty()){
            etCompanyMail.setError("Please Enter Company Email");
            return false;
        }else if (!isValidEmailId(etCompanyMail.getText().toString().trim())){
            etCompanyMail.setError("Please Enter Valid Email");
            return false;
        }else if (!isValidEmailId(etCompanyMail.getText().toString().trim())){
            etCompanyMail.setError("Please Enter Valid Email");
            return false;
        }else if (etPhone.getText().toString().trim().isEmpty()){
            etPhone.setError("Please Enter Company Phone");
            return false;
        }else if (etPhone.getText().toString().trim().length()<10){
            etPhone.setError("Please Enter a Valid Phone Number");
            return false;
        }else if (etCompanyWebsite.getText().toString().trim().isEmpty()){
            etCompanyWebsite.setError("Please Enter Company Website");
            return false;
        }else if (etCompanyWebsite.getText().toString().trim().length()<10){
            etCompanyWebsite.setError("Please Enter Valid Company Website");
            return false;
        }
        return true;
    }
    private boolean isValidEmailId(String email){

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }
    private void uploadFile() {


        if (fileUri != null) {
            final String fileName ="test";

            if (!validateInputFileName(fileName)) {
                return;
            }

            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "/" + fileName);

            createFile(getApplicationContext(), fileUri, file);

            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(s3Client)
                            .build();

            TransferObserver uploadObserver =
                    transferUtility.upload("jsaS3/" + fileName + "." + getFileExtension(fileUri), file);

            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_SHORT).show();

                        file.delete();
                    } else if (TransferState.FAILED == state) {
                        file.delete();
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;

                  //  tvFileName.setText("ID:" + id + "|bytesCurrent: " + bytesCurrent + "|bytesTotal: " + bytesTotal + "|" + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    ex.printStackTrace();
                }

            });
        }
    }

    private void downloadFile() {
        if (fileUri != null) {

            final String fileName = "test";

            if (!validateInputFileName(fileName)) {
                return;
            }

            try {
                final File localFile = File.createTempFile("images", getFileExtension(fileUri));

                TransferUtility transferUtility =
                        TransferUtility.builder()
                                .context(getApplicationContext())
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .s3Client(s3Client)
                                .build();

                TransferObserver downloadObserver =
                        transferUtility.download("jsaS3/" + fileName + "." + getFileExtension(fileUri), localFile);

                downloadObserver.setTransferListener(new TransferListener() {

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            Toast.makeText(getApplicationContext(), "Download Completed!", Toast.LENGTH_SHORT).show();

                          //  tvFileName.setText(fileName + "." + getFileExtension(fileUri));
                            Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bmp);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;

                       // tvFileName.setText("ID:" + id + "|bytesCurrent: " + bytesCurrent + "|bytesTotal: " + bytesTotal + "|" + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        ex.printStackTrace();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Upload file before downloading", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.btn_choose_file) {
            showChoosingFile();
        } else if (i == R.id.btn_upload) {
            uploadFile();
        } /*else if (i == R.id.btn_download) {
            downloadFile();
        }*/
    }

    private void showChoosingFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (bitmap != null) {
            bitmap.recycle();
        }

        if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean validateInputFileName(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}