import { Injectable } from '@angular/core';
import { Http, Response,URLSearchParams} from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { SITE_HOST_URL } from '../../../shared/config/env.config';

import { Post } from '../../model/post.model';

@Injectable()
export class PostlistService {

  //public postListURL = 'mock-data/postlist-mock.json';
  //public postListSearchURL = 'mock-data/postlist-search-mock.json';
  public postListURL = 'blog/post/queryPostListByPage';
  constructor(public http:Http) { }

  public getPostList(searchText: string,page:number=1,categoryId:number=-1):Observable<Post[]>{
    let url = this.postListURL;
    let params = new URLSearchParams();
    params.set('page',String(page));
    params.set('categoryId',String(categoryId));
    params.set('searchText',searchText);
    return this.http
      .get(url,{search:params})
      .map((res:Response) => {
        return res.json();
      })
      .catch((error:any) => Observable.throw(error || 'Server error'));
  }
}

