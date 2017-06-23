import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from "@ngx-translate/core";
import { FormsModule } from "@angular/forms";
import { ModalModule } from "ngx-bootstrap";

import { UserInfoComponent } from "../user/user-info/user-info.component";
import { WritePostComponent } from "../post/write-post/write-post.component";
import { WritePostService } from "../post/write-post/service/write-post.service";
import { SitestatComponent } from "../sitestat/sitestat.component";
import { SiteStatService } from "../sitestat/services/sitestat.service";

@NgModule({
  imports: [
    CommonModule,
    TranslateModule,
    FormsModule,
    ModalModule.forRoot()
  ],
  declarations: [
    UserInfoComponent,
    WritePostComponent,
    SitestatComponent
  ],
  providers:[
    WritePostService,
    SiteStatService
  ],
  exports:[
    UserInfoComponent,
    TranslateModule,
    WritePostComponent,
    SitestatComponent,
    FormsModule
  ]
})
export class SharedModule { }
